(() => {
    const storageKey = "theme";
    const root = document.documentElement;
    let theme = "light";

    try {
        theme = localStorage.getItem(storageKey) || "light";
    } catch (error) {
        // Keep the page usable when browser storage is unavailable.
    }

    root.setAttribute("data-theme", theme);

    let toggle = document.getElementById("themeToggle")
        || document.getElementById("toggleTheme")
        || document.getElementById("themeBtn");

    if (!toggle) {
        toggle = document.createElement("button");
        toggle.type = "button";
        toggle.className = "portfolio-theme-toggle";
        document.body.appendChild(toggle);
    }

    if (toggle.tagName !== "BUTTON") {
        toggle.setAttribute("role", "button");
        toggle.setAttribute("tabindex", "0");
    }

    const updateButton = () => {
        const dark = root.getAttribute("data-theme") === "dark";
        toggle.innerHTML = `<span aria-hidden="true">${dark ? "☀" : "☾"}</span>`;
        toggle.setAttribute("aria-label", dark ? "Switch to light theme" : "Switch to dark theme");
        toggle.setAttribute("title", dark ? "Switch to light theme" : "Switch to dark theme");
    };

    const switchTheme = () => {
        const nextTheme = root.getAttribute("data-theme") === "dark" ? "light" : "dark";
        root.setAttribute("data-theme", nextTheme);
        try {
            localStorage.setItem(storageKey, nextTheme);
        } catch (error) {
            // Keep the selected theme active for the current page.
        }
        updateButton();
    };

    toggle.addEventListener("click", switchTheme);
    if (toggle.tagName !== "BUTTON") {
        toggle.addEventListener("keydown", event => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                switchTheme();
            }
        });
    }
    updateButton();
})();