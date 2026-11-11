// Search feedback only; business rules and all mutations stay on the server.
document.addEventListener("htmx:responseError", () => {
  const notice = document.getElementById("connection-error");
  if (notice) notice.hidden = false;
});
document.addEventListener("htmx:sendError", () => {
  const notice = document.getElementById("connection-error");
  if (notice) notice.hidden = false;
});
document.addEventListener("htmx:afterSwap", () => {
  const notice = document.getElementById("connection-error");
  if (notice) notice.hidden = true;
});
// Prevent accidental double clicks; never retry purchases or payments automatically.
document.addEventListener("submit", (event) => {
  const form = event.target;
  if (form.method !== "post") return;
  const button = event.submitter;
  if (button) {
    button.dataset.originalText = button.textContent;
    button.disabled = true;
    button.textContent = "Procesando…";
  }
});
window.addEventListener("pageshow", () => {
  document.querySelectorAll("button[data-original-text]").forEach((button) => {
    button.disabled = false;
    button.textContent = button.dataset.originalText;
    delete button.dataset.originalText;
  });
});
