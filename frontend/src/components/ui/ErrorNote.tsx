import { motion } from "framer-motion";
import { showTraceRef, type ApiError } from "../../lib/apiError";

type ErrorNoteProps = {
  error: ApiError | null;
};

/**
 * The form-level error banner. Adds a support reference only for failures the user
 * cannot resolve themselves — see showTraceRef — so the ID quoted in a bug report
 * leads straight to that request's backend logs.
 */
export default function ErrorNote({ error }: ErrorNoteProps) {
  if (!error) return null;

  return (
    <motion.div
      initial={{ opacity: 0, y: -6 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500"
    >
      <p>{error.message}</p>
      {showTraceRef(error) && (
        <p className="mt-1.5 font-mono text-xs text-chili-500/70">Ref: {error.traceId}</p>
      )}
    </motion.div>
  );
}
