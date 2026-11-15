
const API = "http://localhost:8080/api";
let authToken = sessionStorage.getItem("lunchpotatoToken");
let currentUser = JSON.parse(
    sessionStorage.getItem("lunchpotatoUser") || "null"
);

let restaurants = [];
let foodItems = [];
let showAllRestaurants = false;
let cartId = null;
let cart = null;

async function apiRequest(url, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (authToken) {
        headers["Authorization"] = `Bearer ${authToken}`;
    }

    const response = await fetch(API + url, {
        ...options,
        headers
    });

    if (!response.ok) {
        const message = await response.text();

        if (response.status === 401 && !url.startsWith("/auth/login")) {
            logout(false);
            openAuthModal("login");
            throw new Error("Your session expired. Please login again.");
        }

        throw new Error(message || "Something went wrong");
    }

    if (response.status === 204) return null;

    return response.json();
}

async function uploadFile(url, file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(API + url, {
        method: "POST",
        headers: authToken
            ? { Authorization: `Bearer ${authToken}` }
            : {},
        body: formData
    });

    if (!response.ok) {
        throw new Error(await response.text() || "Image upload failed");
    }

    return response.json();
}

function notify(message) {
    const element = document.getElementById("notification");
    element.textContent = message;
    element.classList.add("show");

    setTimeout(() => {
        element.classList.remove("show");
    }, 2500);
}

async function loadRestaurants() {
    const container = document.getElementById("restaurant-list");

    try {
        const allRestaurants = await apiRequest("/restaurants");
        restaurants = allRestaurants.filter(restaurant =>
            typeof restaurant.name === "string" &&
            restaurant.name.trim() !== ""
        );

        if (!restaurants.length) {
            container.innerHTML = "<p>No restaurants available yet.</p>";
            document.getElementById("toggle-restaurants").style.display = "none";
            return;
        }

        renderRestaurants();
    } catch (error) {
        container.innerHTML =
            "<p>Unable to load restaurants. Check whether the backend is running.</p>";
        console.error(error);
    }
}

function renderRestaurants() {
    const container = document.getElementById("restaurant-list");
    const button = document.getElementById("toggle-restaurants");
    const visibleRestaurants = showAllRestaurants
        ? restaurants
        : restaurants.slice(0, 4);

    container.innerHTML = visibleRestaurants.map(restaurant => `
        <article class="restaurant-card"
                 role="button"
                 tabindex="0"
                 onclick="selectRestaurant(${restaurant.id})"
                 onkeydown="if(event.key === 'Enter' || event.key === ' '){event.preventDefault();selectRestaurant(${restaurant.id})}">
            ${restaurant.logoUrl
            ? `<img class="restaurant-logo"
                        src="${escapeHTML(restaurant.logoUrl)}"
                        alt="${escapeHTML(restaurant.name)} logo">`
            : ""}
            <div class="restaurant-info">
                <h3>${escapeHTML(restaurant.name)}</h3>
                <p>${escapeHTML(restaurant.address)}</p>
                <p class="rating">
                    ⭐ ${restaurant.rating ?? "New"}
                    · ${restaurant.open ? "Open now" : "Currently closed"}
                </p>
                <p class="restaurant-action">View Menu →</p>
            </div>
        </article>
    `).join("");

    button.style.display = restaurants.length > 4 ? "inline-block" : "none";
    button.textContent = showAllRestaurants
        ? "Show fewer restaurants"
        : "View all restaurants";
}

function toggleRestaurants() {
    showAllRestaurants = !showAllRestaurants;
    renderRestaurants();
}

async function selectRestaurant(restaurantId) {
    const restaurant = restaurants.find(
        item => item.id === restaurantId
    );

    if (!restaurant) return;

    try {
        foodItems = await apiRequest(
            `/food-items/restaurant/${restaurantId}`
        );

        document.getElementById("menu").style.display = "block";
        displayFood(foodItems);
        const heading = document.querySelector(
            "#menu .section-heading h2"
        );

        if (heading) {
            heading.textContent = `${restaurant.name} Menu`;
        }

        document.getElementById("menu").scrollIntoView({
            behavior: "smooth"
        });

    } catch (error) {
        console.error("Restaurant menu error:", error);
        notify("Unable to load this restaurant's menu.");
    }
}

async function loadFoodItems() {
    const container = document.getElementById("food-list");

    try {
        foodItems = await apiRequest("/food-items");
        displayFood(foodItems);
    } catch (error) {
        container.innerHTML = "<p>Unable to load food items.</p>";
        console.error(error);
    }
}

function displayFood(items) {
    const container = document.getElementById("food-list");

    if (!items.length) {
        container.innerHTML = "<p>No food items found.</p>";
        return;
    }

    container.innerHTML = items.map(food => `
        <article class="food-card">
            <img
                src="${escapeHTML(food.imageUrl ||
        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500")}"
                alt="${escapeHTML(food.name)}"
                loading="lazy"
            >

            <div class="food-info">
                <h3>${escapeHTML(food.name)}</h3>
                <p>${escapeHTML(food.description || "Delicious food made fresh.")}</p>
                <div class="price">₹${Number(food.price).toFixed(2)}</div>

                <button
                    class="add-button"
                    onclick="addToCart(${food.id})"
                    ${food.available ? "" : "disabled"}
                >
                    ${food.available ? "Add to cart +" : "Unavailable"}
                </button>
            </div>
        </article>
    `).join("");
}

function filterFood() {
    const search = document.getElementById("food-search")
        .value.toLowerCase();

    const filtered = foodItems.filter(food =>
        food.name.toLowerCase().includes(search)
    );

    displayFood(filtered);
}

function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

async function initializeCart() {
    try {
        const savedCartId = localStorage.getItem("lunchpotatoCartId");

        if (savedCartId) {
            try {
                cartId = Number(savedCartId);
                cart = await apiRequest(`/carts/${cartId}`);
                updateCartUI();
                return;
            } catch {
                localStorage.removeItem("lunchpotatoCartId");
                cartId = null;
            }
        }

        cart = await apiRequest("/carts", {
            method: "POST"
        });

        cartId = cart.id;
        localStorage.setItem("lunchpotatoCartId", cartId);
        updateCartUI();

    } catch (error) {
        notify("Unable to create cart. Check backend.");
        console.error(error);
    }
}

async function addToCart(foodItemId) {
    if (!authToken || !currentUser) {
        notify("Please login before adding food to your cart.");
        openAuthModal("login");
        return;
    }
    if (!cartId) {
        notify("Cart is not ready yet. Please try again.");
        return;
    }

    try {
        cart = await apiRequest(`/carts/${cartId}/items`, {
            method: "POST",
            body: JSON.stringify({
                foodItemId: foodItemId,
                quantity: 1
            })
        });

        updateCartUI();
        notify("Added to your cart!");

    } catch (error) {
        notify("Could not add item to cart.");
        console.error(error);
    }
}

async function updateQuantity(itemId, quantity) {
    if (quantity < 1) {
        await removeItem(itemId);
        return;
    }

    try {
        cart = await apiRequest(
            `/carts/${cartId}/items/${itemId}?quantity=${quantity}`,
            { method: "PUT" }
        );

        updateCartUI();
    } catch (error) {
        notify("Unable to update quantity.");
        console.error(error);
    }
}

async function removeItem(itemId) {
    try {
        cart = await apiRequest(
            `/carts/${cartId}/items/${itemId}`,
            { method: "DELETE" }
        );

        await refreshCart();
        notify("Item removed.");
    } catch (error) {
        notify("Unable to remove item.");
        console.error(error);
    }
}

async function refreshCart() {
    cart = await apiRequest(`/carts/${cartId}`);
    updateCartUI();
}

function updateCartUI() {
    const items = cart?.items || [];
    const count = items.reduce((sum, item) => sum + item.quantity, 0);

    document.getElementById("cart-count").textContent = count;
    document.getElementById("cart-total").textContent =
        "₹" + Number(cart?.totalAmount || 0).toFixed(2);

    const container = document.getElementById("cart-items");

    if (!items.length) {
        container.innerHTML = "<p>Your cart is empty. Add some delicious food!</p>";
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="cart-item">
            <div>
                <strong>${escapeHTML(item.foodItem?.name || "Food item")}</strong>
                <br>
                <small>₹${Number(item.unitPrice).toFixed(2)} each</small>

                <div class="quantity-controls">
                    <button onclick="updateQuantity(${item.id}, ${item.quantity - 1})">−</button>
                    <span>${item.quantity}</span>
                    <button onclick="updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
                </div>
            </div>

            <strong>₹${(item.unitPrice * item.quantity).toFixed(2)}</strong>
        </div>
    `).join("");
}

function toggleCart() {
    if (!authToken || !currentUser) {
        notify("Please login to access your cart.");
        openAuthModal("login");
        return;
    }

    document.getElementById("cart-panel").classList.add("active");
    document.getElementById("cart-overlay").classList.add("active");
}

function closeCart() {
    document.getElementById("cart-panel").classList.remove("active");
    document.getElementById("cart-overlay").classList.remove("active");
}


async function checkout() {
    if (!authToken || !currentUser) {
        notify("Please login before placing an order.");
        openAuthModal("login");
        return;
    }
    if (!cart || !cart.items || cart.items.length === 0) {
        notify("Your cart is empty!");
        return;
    }

    try {
        const order = await apiRequest(`/orders/place/${cartId}`, {
            method: "POST"
        });

        document.getElementById("success-order-id").textContent =
            "#" + order.id;

        document.getElementById("success-order-status").textContent =
            order.status;

        document.getElementById("success-order-total").textContent =
            "₹" + Number(order.totalAmount).toFixed(2);

        localStorage.removeItem("lunchpotatoCartId");

        cart = { items: [], totalAmount: 0 };
        updateCartUI();
        closeCart();

        document.getElementById("order-success")
            .classList.add("active");

        await initializeCart();

    } catch (error) {
        notify(error.message || "Unable to place order. Please try again.");
        console.error(error);
    }
}

function closeSuccess() {
    document.getElementById("order-success")
        .classList.remove("active");
}

function openAuthModal(mode = "login") {
    document.getElementById("auth-modal").classList.add("active");
    switchAuthMode(mode);
}

function closeAuthModal() {
    document.getElementById("auth-modal").classList.remove("active");
}

function switchAuthMode(mode) {
    const loginMode = mode === "login";

    document.getElementById("login-form").style.display =
        loginMode ? "block" : "none";

    document.getElementById("register-form").style.display =
        loginMode ? "none" : "block";

    document.getElementById("auth-title").textContent =
        loginMode ? "Welcome Back!" : "Create Your Account";

    document.getElementById("auth-subtitle").textContent =
        loginMode
            ? "Login to continue with LunchPotato."
            : "Join LunchPotato and discover delicious food.";

    document.getElementById("login-error").textContent = "";
    document.getElementById("register-error").textContent = "";
}

function saveLogin(response) {
    authToken = response.token;

    currentUser = {
        id: response.userId,
        fullName: response.fullName,
        email: response.email,
        role: response.role
    };

    sessionStorage.setItem("lunchpotatoToken", authToken);
    sessionStorage.setItem(
        "lunchpotatoUser",
        JSON.stringify(currentUser)
    );

    updateAuthUI();
}

function updateAuthUI() {
    const loggedIn = Boolean(authToken && currentUser);

    document.getElementById("auth-buttons").style.display =
        loggedIn ? "none" : "flex";

    document.getElementById("user-area").style.display =
        loggedIn ? "flex" : "none";

    document.getElementById("user-name").textContent =
        loggedIn ? `Hi, ${currentUser.fullName}` : "";

    let ownerButton = document.getElementById("owner-dashboard-button");

    if (!ownerButton) {
        ownerButton = document.createElement("button");
        ownerButton.id = "owner-dashboard-button";
        ownerButton.className = "profile-button";
        ownerButton.textContent = "Owner Dashboard";
        ownerButton.onclick = openOwnerDashboard;

        document.getElementById("user-area")
            .appendChild(ownerButton);
    }

    ownerButton.style.display =
        loggedIn && currentUser.role === "RESTAURANT_OWNER"
            ? "inline-block"
            : "none";

    document.getElementById("owner-dashboard").style.display = "none";
    let adminButton = document.getElementById("admin-dashboard-button");

    if (!adminButton) {
        adminButton = document.createElement("button");
        adminButton.id = "admin-dashboard-button";
        adminButton.className = "profile-button";
        adminButton.textContent = "Admin Dashboard";
        adminButton.onclick = openAdminDashboard;

        document.getElementById("user-area").appendChild(adminButton);
    }

    adminButton.style.display =
        loggedIn && currentUser.role === "ADMIN"
            ? "inline-block"
            : "none";

    document.getElementById("admin-dashboard").style.display = "none";
}

async function handleLogin(event) {
    event.preventDefault();

    const email = document.getElementById("login-email").value;
    const password = document.getElementById("login-password").value;
    const errorElement = document.getElementById("login-error");

    errorElement.textContent = "";

    try {
        const response = await apiRequest("/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });

        saveLogin(response);
        closeAuthModal();

        // Do not reuse an old guest cart
        localStorage.removeItem("lunchpotatoCartId");

        if (currentUser.role === "CUSTOMER") {
            await initializeCart();
        } else if (currentUser.role === "RESTAURANT_OWNER") {
            await openOwnerDashboard();
        }

        notify("Welcome to LunchPotato!");

    } catch (error) {
        errorElement.textContent = "Login failed. Check your email and password.";
        console.error(error);
    }
}

async function handleRegister(event) {
    event.preventDefault();

    const fullName = document.getElementById("register-name").value;
    const email = document.getElementById("register-email").value;
    const phone = document.getElementById("register-phone").value;
    const password = document.getElementById("register-password").value;
    const address = document.getElementById("register-address").value.trim();
    const errorElement = document.getElementById("register-error");

    errorElement.textContent = "";

    try {
        await apiRequest("/users/register", {
            method: "POST",
            body: JSON.stringify({
                fullName,
                email,
                password,
                address
            })
        });

        // Automatically login after successful registration
        const response = await apiRequest("/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });

        saveLogin(response);
        closeAuthModal();

        // Do not reuse an old guest cart
        localStorage.removeItem("lunchpotatoCartId");

        await initializeCart();

        notify("Account created successfully!");

    } catch (error) {
        errorElement.textContent =
            "Registration failed. " + error.message;

        console.error(error);
    }
}

async function openProfile() {
    document.getElementById("profile-modal").classList.add("active");

    document.getElementById("profile-error").textContent = "";
    document.getElementById("profile-orders").innerHTML =
        "<p>Loading your orders...</p>";

    await Promise.all([
        loadProfile(),
        loadProfileOrders()
    ]);
}
function closeProfile() {
    document.getElementById("profile-modal").classList.remove("active");
}
async function loadProfile() {
    try {
        const profile = await apiRequest("/profile");

        document.getElementById("profile-name").value =
            profile.fullName || "";

        document.getElementById("profile-email").value =
            profile.email || "";

        document.getElementById("profile-phone").value =
            profile.phone || "";

        document.getElementById("profile-address").value =
            profile.address || "";

    } catch (error) {
        document.getElementById("profile-error").textContent =
            "Unable to load profile information.";

        console.error(error);
    }
}

async function saveProfile(event) {
    event.preventDefault();

    const fullName = document.getElementById("profile-name").value.trim();
    const phone = document.getElementById("profile-phone").value.trim();
    const errorElement = document.getElementById("profile-error");
    const address = document.getElementById("profile-address").value.trim();

    errorElement.textContent = "";

    if (fullName.length < 2 || !/^[0-9]{10}$/.test(phone) || address.length < 5) {
        errorElement.textContent =
            "Enter a valid name, 10-digit phone number, and delivery address.";
        return;
    }

    try {
        const updatedProfile = await apiRequest("/profile", {
            method: "PUT",
            body: JSON.stringify({
                fullName: fullName,
                phone: phone,
                address: address
            })
        });

        currentUser.fullName = updatedProfile.fullName;

        sessionStorage.setItem(
            "lunchpotatoUser",
            JSON.stringify(currentUser)
        );

        updateAuthUI();

        notify("Profile updated successfully!");

    } catch (error) {
        errorElement.textContent =
            "Unable to update profile. " + error.message;

        console.error(error);
    }
}

async function loadProfileOrders() {
    const container = document.getElementById("profile-orders");

    try {
        const orders = await apiRequest("/orders/my-orders");

        if (!orders || orders.length === 0) {
            container.innerHTML = `
                <div class="empty-orders">
                    <p>You haven't placed any orders yet.</p>
                    <p>Explore our menu and order something delicious!</p>
                </div>
            `;
            return;
        }

        container.innerHTML = orders.map(order => {
            const orderDate = order.createdAt
                ? new Date(order.createdAt).toLocaleString("en-IN")
                : "Date unavailable";

            const items = (order.items || []).map(item => `
                <div class="profile-order-item">
                    <span>
                        ${escapeHTML(item.foodName)}
                        × ${item.quantity}
                    </span>

                    <strong>
                        ₹${(Number(item.unitPrice) * item.quantity).toFixed(2)}
                    </strong>
                </div>
            `).join("");

            return `
                <article class="profile-order-card">

                    <div class="profile-order-header">
                        <strong>Order #${order.id}</strong>

                        <span class="order-status">
                            ${escapeHTML(order.status)}
                        </span>
                    </div>

                    <p class="order-date">${escapeHTML(orderDate)}</p>

                    <div class="profile-order-items">
                        ${items || "<p>No item details available.</p>"}
                    </div>

                    <div class="profile-order-total">
                        <span>Total Amount</span>
                        <strong>
                            ₹${Number(order.totalAmount || 0).toFixed(2)}
                        </strong>
                    </div>

                </article>
            `;
        }).join("");

    } catch (error) {
        container.innerHTML =
            "<p>Unable to load your order history.</p>";

        console.error(error);
    }
}

function logout(showMessage = true) {
    authToken = null;
    currentUser = null;
    cartId = null;
    cart = null;

    sessionStorage.removeItem("lunchpotatoToken");
    sessionStorage.removeItem("lunchpotatoUser");
    localStorage.removeItem("lunchpotatoCartId");

    document.getElementById("owner-dashboard").style.display = "none";
    document.getElementById("admin-dashboard").style.display = "none";
    updateAuthUI();
    updateCartUI();

    if (showMessage) {
        notify("You have been logged out.");
    }
}

async function initializeWebsite() {
    updateAuthUI();

    await loadRestaurants();

    // Only create/load a cart when the customer is logged in
    if (authToken && currentUser &&
        currentUser.role === "CUSTOMER") {

        await initializeCart();

    } else {
        cart = { items: [], totalAmount: 0 };
        updateCartUI();
    }
}

// ===============================
// RESTAURANT OWNER DASHBOARD
// ===============================

let ownerRestaurants = [];
let ownerMenuItems = [];
let ownerOrders = [];

async function openOwnerDashboard() {

    if (!currentUser ||
        currentUser.role !== "RESTAURANT_OWNER") {
        notify("Access denied.");
        return;
    }

    document.getElementById("owner-dashboard").style.display = "block";

    document.getElementById("owner-dashboard")
        .scrollIntoView({ behavior: "smooth" });

    await refreshOwnerDashboard();
}

async function refreshOwnerDashboard() {

    if (!currentUser ||
        currentUser.role !== "RESTAURANT_OWNER") {
        return;
    }

    try {
        const [restaurants, menu, orders] = await Promise.all([
            apiRequest("/owner/restaurants"),
            apiRequest("/owner/menu"),
            apiRequest("/owner/orders")
        ]);

        ownerRestaurants = restaurants;
        ownerMenuItems = menu;
        ownerOrders = orders;

        displayOwnerRestaurants();
        displayOwnerMenu();
        displayOwnerOrders();

    } catch (error) {
        console.error("Owner dashboard error:", error);
        notify("Unable to load owner dashboard.");
    }
}

function displayOwnerRestaurants() {

    const container = document.getElementById("owner-restaurants");
    const select = document.getElementById("owner-restaurant-select");

    if (!ownerRestaurants.length) {
        container.innerHTML =
            '<p class="owner-empty">No restaurants assigned to you.</p>';

        select.innerHTML =
            '<option value="">No restaurants available</option>';

        return;
    }

    container.innerHTML = ownerRestaurants.map(restaurant => `
        <div class="owner-card">
            <h3>${escapeHTML(restaurant.name)}</h3>
            <p>${escapeHTML(restaurant.address)}</p>
            <p>Rating: ${restaurant.rating ?? "Not rated"}</p>
            <p>Status: ${restaurant.open ? "Open" : "Closed"}</p>
            <p>Restaurant ID: ${restaurant.id}</p>
        </div>
    `).join("");

    select.innerHTML = ownerRestaurants.map(restaurant => `
        <option value="${restaurant.id}">
            ${escapeHTML(restaurant.name)}
        </option>
    `).join("");
}

async function addOwnerFood(event) {

    event.preventDefault();

    const restaurantId =
        Number(document.getElementById("owner-restaurant-select").value);

    const food = {
        restaurantId,
        name: document.getElementById("owner-food-name").value.trim(),
        description: document.getElementById("owner-food-description").value.trim(),
        price: Number(document.getElementById("owner-food-price").value),
        available: document.getElementById("owner-food-available").value === "true"
    };

    if (!restaurantId || !food.name || !Number.isFinite(food.price) ||
        food.price <= 0) {
        notify("Enter valid food details.");
        return;
    }

    const imageFile = document.getElementById("owner-food-image").files[0];

    if (!imageFile) {
        notify("Choose a picture for this food item.");
        return;
    }

    try {
        const createdFood = await apiRequest("/owner/menu", {
            method: "POST",
            body: JSON.stringify(food)
        });

        await uploadFile(`/owner/menu/${createdFood.id}/image`, imageFile);

        document.getElementById("owner-food-form").reset();

        notify("Food item added successfully.");

        await refreshOwnerDashboard();

    } catch (error) {
        console.error(error);
        notify("Unable to add food item.");
    }
}

function displayOwnerMenu() {

    const container = document.getElementById("owner-menu");

    if (!ownerMenuItems.length) {
        container.innerHTML =
            '<p class="owner-empty">No food items found.</p>';
        return;
    }

    container.innerHTML = ownerMenuItems.map(item => `
        <div class="owner-card">
        ${item.imageUrl
            ? `<img class="dashboard-image" src="${escapeHTML(item.imageUrl)}"
            alt="${escapeHTML(item.name)}">`
            : "<p>Picture not uploaded</p>"}
            <h3>${escapeHTML(item.name)}</h3>

            <p>${escapeHTML(item.description || "No description")}</p>

            <p>Price: ₹${Number(item.price).toFixed(2)}</p>

            <p>Availability:
                ${item.available ? "Available" : "Unavailable"}
            </p>

            <button class="primary-button"
                onclick="editOwnerFood(${item.id})">
                Edit
            </button>

            <button class="primary-button"
                onclick="deleteOwnerFood(${item.id})">
                Delete
            </button>
        </div>
    `).join("");
}

async function editOwnerFood(foodId) {

    const item = ownerMenuItems.find(food => food.id === foodId);

    if (!item) return;

    const name = prompt("Food name:", item.name);
    if (name === null) return;

    const priceInput = prompt("Price:", item.price);
    if (priceInput === null) return;

    const price = Number(priceInput);

    if (!name.trim() || !Number.isFinite(price) || price <= 0) {
        notify("Invalid food details.");
        return;
    }

    const description = prompt("Description:", item.description || "");
    if (description === null) return;

    const available = confirm(
        "Press OK if this food item is available. Cancel if unavailable."
    );

    const restaurantId = item.restaurant?.id ??
        Number(document.getElementById("owner-restaurant-select").value);

    if (!restaurantId) {
        notify("Unable to identify restaurant.");
        return;
    }

    try {
        await apiRequest(`/owner/menu/${foodId}`, {
            method: "PUT",
            body: JSON.stringify({
                restaurantId,
                name: name.trim(),
                description: description.trim(),
                price,
                available
            })
        });

        notify("Food item updated.");
        await refreshOwnerDashboard();

    } catch (error) {
        console.error(error);
        notify("Unable to update food item.");
    }
}

async function deleteOwnerFood(foodId) {

    if (!confirm("Are you sure you want to delete this food item?")) {
        return;
    }

    try {
        await apiRequest(`/owner/menu/${foodId}`, {
            method: "DELETE"
        });

        notify("Food item deleted.");
        await refreshOwnerDashboard();

    } catch (error) {
        console.error(error);
        notify("Unable to delete food item.");
    }
}

function displayOwnerOrders() {

    const container = document.getElementById("owner-orders");

    if (!ownerOrders.length) {
        container.innerHTML =
            '<p class="owner-empty">No orders received yet.</p>';
        return;
    }

    const statuses = [
        "PLACED",
        "CONFIRMED",
        "PREPARING",
        "OUT_FOR_DELIVERY",
        "DELIVERED",
        "CANCELLED"
    ];

    container.innerHTML = ownerOrders.map(order => `
        <div class="owner-card">
            <h3>Order #${order.id}</h3>

            <p>Status: ${escapeHTML(order.status)}</p>

            <p>Total: ₹${Number(order.totalAmount || 0).toFixed(2)}</p>

            <p>Items:
                ${(order.items || []).map(item =>
        `${escapeHTML(item.foodName)} × ${item.quantity}`
    ).join(", ")}
            </p>

            <label>Update Status</label>

            <select id="owner-order-status-${order.id}">
                ${statuses.map(status => `
                    <option value="${status}"
                        ${order.status === status ? "selected" : ""}>
                        ${status.replaceAll("_", " ")}
                    </option>
                `).join("")}
            </select>

            <button class="primary-button"
                onclick="updateOwnerOrderStatus(${order.id})">
                Update Order
            </button>
        </div>
    `).join("");
}

async function updateOwnerOrderStatus(orderId) {

    const status = document.getElementById(
        `owner-order-status-${orderId}`
    ).value;

    try {
        await apiRequest(
            `/owner/orders/${orderId}/status?status=${encodeURIComponent(status)}`,
            { method: "PUT" }
        );

        notify("Order status updated.");
        await refreshOwnerDashboard();

    } catch (error) {
        console.error(error);
        notify("Unable to update order status.");
    }
}


// ===============================
// ADMIN DASHBOARD
// ===============================

async function openAdminDashboard() {
    if (!currentUser || currentUser.role !== "ADMIN") {
        notify("Access denied.");
        return;
    }

    document.getElementById("owner-dashboard").style.display = "none";

    const dashboard = document.getElementById("admin-dashboard");
    dashboard.style.display = "block";
    dashboard.scrollIntoView({ behavior: "smooth" });

    await refreshAdminDashboard();
}

async function refreshAdminDashboard() {
    if (!currentUser || currentUser.role !== "ADMIN") return;

    try {
        const [overview, restaurants] = await Promise.all([
            apiRequest("/admin/overview"),
            apiRequest("/admin/restaurants")
        ]);

        document.getElementById("admin-total-users").textContent =
            overview.totalUsers;

        document.getElementById("admin-total-restaurants").textContent =
            overview.totalRestaurants;

        document.getElementById("admin-total-orders").textContent =
            overview.totalOrders;

        displayAdminRestaurants(restaurants);

    } catch (error) {
        console.error("Admin dashboard error:", error);
        notify("Unable to load admin dashboard.");
    }
}

async function addRestaurant(event) {
    event.preventDefault();

    const name = document.getElementById("new-restaurant-name").value.trim();
    const address = document.getElementById("new-restaurant-address").value.trim();
    const ratingValue = document.getElementById("new-restaurant-rating").value;
    const open = document.getElementById("new-restaurant-open").checked;
    const logoFile = document.getElementById("new-restaurant-logo").files[0];

    if (!name || !address || !logoFile) {
        notify("Enter restaurant details and choose a logo.");
        return;
    }

    try {
        const restaurant = await apiRequest("/restaurants", {
            method: "POST",
            body: JSON.stringify({
                name,
                address,
                rating: ratingValue === "" ? null : Number(ratingValue),
                open
            })
        });

        await uploadFile(
            `/admin/restaurants/${restaurant.id}/logo`,
            logoFile
        );

        document.getElementById("admin-restaurant-form").reset();
        document.getElementById("new-restaurant-open").checked = true;

        notify("Restaurant and logo added.");
        await refreshAdminDashboard();
        await loadRestaurants();
    } catch (error) {
        console.error("Add restaurant error:", error);
        notify(error.message || "Unable to add restaurant.");
    }
}

function displayAdminRestaurants(restaurants) {
    const container = document.getElementById("admin-restaurants");
    const select = document.getElementById("admin-restaurant-select");

    if (!restaurants || restaurants.length === 0) {
        container.innerHTML = '<p class="owner-empty">No restaurants found.</p>';
        select.innerHTML = '<option value="">No restaurants available</option>';
        return;
    }

    container.innerHTML = restaurants.map(restaurant => `
        <div class="owner-card">
            ${restaurant.logoUrl
            ? `<img class="dashboard-image"
                        src="${escapeHTML(restaurant.logoUrl)}"
                        alt="${escapeHTML(restaurant.name)} logo">`
            : ""}
            <h3>${escapeHTML(restaurant.name)}</h3>

            <label>Name</label>
            <input id="admin-name-${restaurant.id}"
                   value="${escapeHTML(restaurant.name)}">

            <label>Address</label>
            <input id="admin-address-${restaurant.id}"
                   value="${escapeHTML(restaurant.address)}">

            <label>Rating</label>
            <input id="admin-rating-${restaurant.id}" type="number"
                   min="0" max="5" step="0.1"
                   value="${restaurant.rating ?? ""}">

            <label>
                <input id="admin-open-${restaurant.id}" type="checkbox"
                    ${restaurant.open ? "checked" : ""}>
                Restaurant is open
            </label>

            <button class="primary-button"
                    onclick="saveRestaurant(${restaurant.id})">
                Save Details
            </button>

            <label for="admin-logo-${restaurant.id}">Change Logo</label>
            <input id="admin-logo-${restaurant.id}" type="file"
                   accept="image/jpeg,image/png,image/gif">
            <button class="primary-button"
                    onclick="uploadRestaurantLogo(${restaurant.id})">
                Upload Logo
            </button>

            <p>Owner: ${restaurant.ownerName
            ? escapeHTML(restaurant.ownerName) : "Not assigned"}</p>
            <p>Email: ${restaurant.ownerEmail
            ? escapeHTML(restaurant.ownerEmail) : "Not assigned"}</p>
        </div>
    `).join("");

    select.innerHTML = '<option value="">Select restaurant</option>' +
        restaurants.map(restaurant => `
            <option value="${restaurant.id}">
                ${escapeHTML(restaurant.name)} (ID: ${restaurant.id})
            </option>
        `).join("");
}

async function saveRestaurant(id) {
    const ratingValue = document.getElementById(`admin-rating-${id}`).value;

    const body = {
        name: document.getElementById(`admin-name-${id}`).value.trim(),
        address: document.getElementById(`admin-address-${id}`).value.trim(),
        rating: ratingValue === "" ? null : Number(ratingValue),
        open: document.getElementById(`admin-open-${id}`).checked
    };

    if (!body.name || !body.address) {
        notify("Restaurant name and address are required.");
        return;
    }

    try {
        await apiRequest(`/admin/restaurants/${id}`, {
            method: "PUT",
            body: JSON.stringify(body)
        });
        notify("Restaurant details updated.");
        await refreshAdminDashboard();
        await loadRestaurants();
    } catch (error) {
        notify(error.message || "Unable to update restaurant.");
    }
}

async function uploadRestaurantLogo(id) {
    const file = document.getElementById(`admin-logo-${id}`).files[0];

    if (!file) {
        notify("Choose a logo image first.");
        return;
    }

    try {
        await uploadFile(`/admin/restaurants/${id}/logo`, file);
        notify("Restaurant logo updated.");
        await refreshAdminDashboard();
        await loadRestaurants();
    } catch (error) {
        notify(error.message || "Unable to upload logo.");
    }
}

async function assignRestaurantOwner(event) {
    event.preventDefault();

    const restaurantId =
        Number(document.getElementById("admin-restaurant-select").value);

    const email =
        document.getElementById("admin-owner-email").value.trim();

    if (!restaurantId || !email) {
        notify("Select a restaurant and enter an email.");
        return;
    }

    try {
        await apiRequest(
            `/admin/restaurants/${restaurantId}/owner`,
            {
                method: "PUT",
                body: JSON.stringify({ email })
            }
        );

        document.getElementById("admin-owner-form").reset();

        notify("Restaurant owner assigned successfully.");

        await refreshAdminDashboard();

    } catch (error) {
        console.error("Owner assignment error:", error);
        notify("Unable to assign owner. Check the email and assignment.");
    }
}


initializeWebsite();