document.addEventListener('DOMContentLoaded', function () {
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (window.AOS) {
        AOS.init({
            duration: reduceMotion ? 0 : 700,
            once: true,
            offset: 40,
            disable: reduceMotion
        });
    }

    const typedElement = document.getElementById('typed-text');
    if (typedElement && window.Typed && !reduceMotion) {
        new Typed('#typed-text', {
            strings: ['Java Backend Engineer', 'Spring Boot', 'Backend Systems', 'API Development'],
            typeSpeed: 42,
            backSpeed: 24,
            backDelay: 1500,
            loop: true,
            showCursor: false
        });
    }

    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        let savedTheme = 'light';
        try {
            savedTheme = localStorage.getItem('theme') || 'light';
        } catch (error) {
            // Theme preference remains usable for this session when storage is unavailable.
        }
        document.documentElement.setAttribute('data-theme', savedTheme);

        const updateThemeButton = (theme) => {
            const icon = themeToggle.querySelector('i');
            if (icon) icon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
            themeToggle.setAttribute('aria-label', theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme');
        };
        updateThemeButton(savedTheme);

        themeToggle.addEventListener('click', function () {
            const nextTheme = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', nextTheme);
            try {
                localStorage.setItem('theme', nextTheme);
            } catch (error) {
                // Keep the selected theme active even if browser storage is blocked.
            }
            updateThemeButton(nextTheme);
        });
    }

    const wireJsonForm = (formId, statusId, endpoint, fields, successMessage, errorMessage) => {
        const form = document.getElementById(formId);
        const status = document.getElementById(statusId);
        if (!form) return;

        form.addEventListener('submit', async function (event) {
            event.preventDefault();
            if (!form.reportValidity()) return;

            const submitButton = form.querySelector('[type="submit"]');
            const originalLabel = submitButton ? submitButton.innerHTML : '';
            const payload = {};
            fields.forEach((field) => {
                const input = form.elements.namedItem(field);
                payload[field] = input ? input.value : '';
            });
            if (status) {
                status.textContent = '';
                status.classList.remove('is-error');
            }
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.setAttribute('aria-busy', 'true');
                submitButton.textContent = 'Sending…';
            }

            try {
                const response = await fetch(endpoint, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!response.ok) throw new Error('Request failed');
                if (status) status.textContent = successMessage;
                form.reset();
            } catch (error) {
                if (status) {
                    status.textContent = errorMessage;
                    status.classList.add('is-error');
                }
            } finally {
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.removeAttribute('aria-busy');
                    submitButton.innerHTML = originalLabel;
                }
            }
        });
    };

    wireJsonForm(
        'contactForm',
        'contactStatus',
        '/contact',
        ['name', 'email', 'message'],
        'Thanks — your message has been sent.',
        'Your message could not be sent. Please try again.'
    );
    wireJsonForm(
        'meetingForm',
        'meetingStatus',
        '/meeting/request',
        ['name', 'email', 'phone', 'preferredDatetime', 'type', 'message'],
        'Thanks — your meeting request has been submitted.',
        'Your request could not be submitted. Please try again.'
    );

    document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
        anchor.addEventListener('click', function (event) {
            const selector = this.getAttribute('href');
            if (!selector || selector === '#') return;
            const target = document.querySelector(selector);
            if (!target) return;
            event.preventDefault();
            target.scrollIntoView({
                behavior: reduceMotion ? 'auto' : 'smooth',
                block: 'start'
            });
            const navCollapse = document.getElementById('navbarNav');
            if (navCollapse && navCollapse.classList.contains('show') && window.bootstrap) {
                window.bootstrap.Collapse.getOrCreateInstance(navCollapse).hide();
            }
        });
    });

    const chatBox = document.getElementById('chatBox');
    const chatToggle = document.getElementById('chatToggle');
    const chatIcon = document.getElementById('chatIcon');
    const chatInput = document.getElementById('chatInput');
    const chatSend = document.getElementById('chatSend');
    const chatBody = document.getElementById('chatBody');

    if (chatBox && chatToggle && chatIcon && chatInput && chatSend && chatBody) {
        chatToggle.addEventListener('click', () => {
            const isOpen = chatBox.classList.toggle('collapsed') === false;
            chatToggle.setAttribute('aria-expanded', String(isOpen));
            chatIcon.className = isOpen ? 'fas fa-chevron-up' : 'fas fa-chevron-down';
            if (isOpen) chatInput.focus();
        });

        const appendMessage = (message, type = 'bot') => {
            const element = document.createElement('div');
            element.classList.add(type === 'user' ? 'user-msg' : 'bot-msg');
            element.textContent = message;
            chatBody.appendChild(element);
            chatBody.scrollTop = chatBody.scrollHeight;
            return element;
        };

        const sendMessage = async () => {
            const message = chatInput.value.trim();
            if (!message || chatSend.disabled) return;
            appendMessage(message, 'user');
            chatInput.value = '';
            chatSend.disabled = true;

            const typingElement = document.createElement('div');
            typingElement.className = 'bot-msg typing';
            typingElement.setAttribute('aria-label', 'Assistant is typing');
            for (let index = 0; index < 3; index += 1) {
                typingElement.appendChild(document.createElement('span'));
            }
            chatBody.appendChild(typingElement);
            chatBody.scrollTop = chatBody.scrollHeight;

            try {
                const response = await fetch('/api/chat/message', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({ message })
                });
                typingElement.remove();
                if (!response.ok) {
                    appendMessage(`The server returned an error (${response.status}). Please try again.`);
                    return;
                }

                const data = await response.json();
                if (data.type === 'text') {
                    appendMessage(data.response || 'No response was returned.');
                } else if (data.type === 'image') {
                    const element = document.createElement('div');
                    element.className = 'bot-msg';
                    element.appendChild(document.createTextNode(data.prompt || 'Generated image'));
                    if (data.imageUrl) {
                        const image = document.createElement('img');
                        image.src = data.imageUrl;
                        image.alt = data.prompt || 'Generated image';
                        image.loading = 'lazy';
                        element.appendChild(image);
                    }
                    chatBody.appendChild(element);
                } else {
                    appendMessage('I could not understand that response. Please try again.');
                }
                chatBody.scrollTop = chatBody.scrollHeight;
            } catch (error) {
                typingElement.remove();
                appendMessage('Something went wrong. Please try again.');
            } finally {
                chatSend.disabled = false;
                chatInput.focus();
            }
        };

        chatInput.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                sendMessage();
            }
        });
        chatSend.addEventListener('click', sendMessage);
    }
});