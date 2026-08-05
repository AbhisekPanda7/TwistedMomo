import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { BellIcon } from "../ui/Icons";
import { useAuth } from "../../context/AuthContext";
import {
  fetchNotifications,
  fetchUnreadCount,
  markNotificationRead,
  type Notification,
} from "../../lib/notifications";

const POLL_INTERVAL_MS = 30000;

function timeAgo(iso: string): string {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  if (seconds < 60) return "just now";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

export default function NotificationBell() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [items, setItems] = useState<Notification[]>([]);
  const containerRef = useRef<HTMLDivElement>(null);

  // Signed-out users must never poll: firing without a token just farms 401s
  // and can trip the api instance's refresh-and-retry logic.
  useEffect(() => {
    if (!user) {
      setUnreadCount(0);
      setItems([]);
      return;
    }

    let cancelled = false;
    function refreshCount() {
      fetchUnreadCount()
        .then((count) => {
          if (!cancelled) setUnreadCount(count);
        })
        .catch(() => {
          // A background poll failing shouldn't surface an error to the customer.
        });
    }

    refreshCount();
    const intervalId = setInterval(refreshCount, POLL_INTERVAL_MS);
    return () => {
      cancelled = true;
      clearInterval(intervalId);
    };
  }, [user]);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    if (open) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  function handleToggle() {
    const next = !open;
    setOpen(next);
    if (next) {
      fetchNotifications(0, 20)
        .then((page) => setItems(page.content))
        .catch(() => {
          // Leave the previous feed in place rather than blanking it on a failed refresh.
        });
    }
  }

  async function handleItemClick(notification: Notification) {
    if (!notification.read) {
      try {
        const updated = await markNotificationRead(notification.id);
        setItems((prev) => prev.map((n) => (n.id === updated.id ? updated : n)));
        setUnreadCount((prev) => Math.max(0, prev - 1));
      } catch {
        // Navigation below still proceeds even if marking read failed server-side.
      }
    }
    setOpen(false);
    // Notifications without an orderId (e.g. future coupon types) have nowhere to navigate.
    if (notification.orderId != null) {
      navigate(`/orders/${notification.orderId}`);
    }
  }

  if (!user) return null;

  return (
    <div ref={containerRef} className="relative">
      <button
        onClick={handleToggle}
        data-cursor-hover
        aria-label="Notifications"
        className="relative text-paper-100 transition-colors hover:text-gold-400"
      >
        <BellIcon className="h-6 w-6" />
        {unreadCount > 0 && (
          <span className="absolute -right-2 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-gold-400 px-1 font-sans text-[10px] font-bold text-ink-950">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.15 }}
            className="absolute right-0 top-full z-50 mt-3 w-80 overflow-hidden rounded-2xl border border-ink-600 bg-ink-900 shadow-xl"
          >
            <div className="max-h-96 overflow-y-auto">
              {items.length === 0 ? (
                <p className="px-5 py-6 text-center font-sans text-sm text-paper-200/60">
                  No notifications yet.
                </p>
              ) : (
                items.map((notification) => (
                  <button
                    key={notification.id}
                    onClick={() => handleItemClick(notification)}
                    data-cursor-hover
                    className={`block w-full border-b border-ink-700 px-5 py-3 text-left transition-colors last:border-b-0 hover:bg-ink-700 ${
                      notification.read ? "" : "bg-gold-400/5"
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <span className="font-sans text-sm font-semibold text-paper-50">{notification.title}</span>
                      {!notification.read && <span className="mt-1 h-2 w-2 shrink-0 rounded-full bg-gold-400" />}
                    </div>
                    <p className="mt-1 font-sans text-xs text-paper-200/70">{notification.body}</p>
                    <p className="mt-1 font-sans text-[10px] uppercase tracking-wider text-paper-200/40">
                      {timeAgo(notification.createdAt)}
                    </p>
                  </button>
                ))
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
