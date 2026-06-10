import type { StepStatus } from "../api/client";

const labels: Record<StepStatus, string> = {
  pending: "Pending",
  running: "Running",
  success: "Done",
  error: "Failed",
  warning: "Warning",
};

interface StatusBadgeProps {
  status: StepStatus;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  return <span className={`badge badge-${status}`}>{labels[status]}</span>;
}
