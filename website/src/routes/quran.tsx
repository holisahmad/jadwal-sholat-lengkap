import { createFileRoute, Outlet } from "@tanstack/react-router";

export const Route = createFileRoute("/quran")({
  component: QuranLayout,
});

function QuranLayout() {
  return <Outlet />;
}
