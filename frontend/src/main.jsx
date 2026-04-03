import React from "react";
import ReactDOM from "react-dom/client";
import { HelmetProvider } from "react-helmet-async";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { ToastProvider } from "@/components/global/ToastProvider";
import "./styles/globals.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <HelmetProvider>
      <BrowserRouter>
        <App />
        <ToastProvider />
      </BrowserRouter>
    </HelmetProvider>
  </React.StrictMode>
);



