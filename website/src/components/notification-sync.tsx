import { useEffect } from "react";
import { cityById, useNur } from "@/lib/store";
import { scheduleNotifications } from "@/lib/notification";

export function NotificationSync() {
  const cityId = useNur((s) => s.cityId);
  const method = useNur((s) => s.method);
  const notif = useNur((s) => s.notif);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const city = cityById(cityId);
    scheduleNotifications(city, method, notif);
  }, [cityId, method, notif]);

  return null;
}
