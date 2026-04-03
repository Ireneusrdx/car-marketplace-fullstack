import { Toaster, toast } from "react-hot-toast";

export function ToastProvider() {
  return (
    <Toaster
      position="top-right"
      toastOptions={{
        style: {
          borderRadius: "14px",
          background: "#FFFFFF",
          color: "#0D1117",
          border: "1px solid rgba(0,87,255,0.2)",
          boxShadow: "0 4px 24px rgba(0,87,255,0.12), 0 2px 8px rgba(0,0,0,0.08)",
          fontWeight: 500
        },
        success: {
          iconTheme: {
            primary: "#0057FF",
            secondary: "#FFFFFF"
          }
        },
        error: {
          iconTheme: {
            primary: "#FF3D57",
            secondary: "#FFFFFF"
          }
        }
      }}
    />
  );
}

export const notify = {
  success: (message) => toast.success(message),
  error: (message) => toast.error(message),
  info: (message) => toast(message)
};

