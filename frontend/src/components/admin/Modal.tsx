import type { ReactNode } from "react";
import { AnimatePresence, motion } from "framer-motion";

export default function Modal({
  open,
  onClose,
  title,
  children,
}: {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
}) {
  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-[100] flex items-center justify-center bg-ink-950/80 p-4 backdrop-blur-sm"
          onClick={onClose}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.96, y: 12 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.96, y: 12 }}
            transition={{ duration: 0.2 }}
            onClick={(e) => e.stopPropagation()}
            className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-3xl border border-ink-600 bg-ink-900 p-7 sm:p-8"
          >
            <div className="mb-5 flex items-center justify-between">
              <h3 className="font-display text-xl uppercase tracking-wide text-gold-400">{title}</h3>
              <button
                onClick={onClose}
                data-cursor-hover
                aria-label="Close"
                className="flex h-8 w-8 items-center justify-center rounded-full text-paper-200/50 transition-colors hover:bg-ink-700 hover:text-paper-50"
              >
                ✕
              </button>
            </div>
            {children}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
