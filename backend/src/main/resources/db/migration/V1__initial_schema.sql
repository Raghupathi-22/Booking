-- Hyderabad Booking Marketplace Initial Schema V1

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'PARTNER', 'ADMIN')),
    fcm_token VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE partners (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('SALON', 'TUTOR')),
    business_name VARCHAR(150) NOT NULL,
    description TEXT,
    address TEXT,
    lat DECIMAL(10, 8),
    lng DECIMAL(11, 8),
    area VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    commission_percent DECIMAL(5, 2) DEFAULT 10.00,
    rating_avg DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INT DEFAULT 0
);

CREATE TABLE partner_documents (
    id SERIAL PRIMARY KEY,
    partner_id INT REFERENCES partners(id) ON DELETE CASCADE,
    doc_type VARCHAR(50) NOT NULL,
    url VARCHAR(255) NOT NULL
);

CREATE TABLE services (
    id SERIAL PRIMARY KEY,
    partner_id INT REFERENCES partners(id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL CHECK (category IN ('SALON', 'TUITION')),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    mode VARCHAR(20) NOT NULL CHECK (mode IN ('AT_SHOP', 'AT_HOME', 'ONLINE')),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE working_hours (
    id SERIAL PRIMARY KEY,
    partner_id INT REFERENCES partners(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL
);

CREATE TABLE time_off (
    id SERIAL PRIMARY KEY,
    partner_id INT REFERENCES partners(id) ON DELETE CASCADE,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL
);

CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES users(id) ON DELETE CASCADE,
    partner_id INT REFERENCES partners(id) ON DELETE CASCADE,
    service_id INT REFERENCES services(id) ON DELETE CASCADE,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    booking_type VARCHAR(20) DEFAULT 'SINGLE' CHECK (booking_type IN ('SINGLE', 'DEMO', 'RECURRING')),
    recurrence_rule VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    commission_amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    booking_id INT REFERENCES bookings(id) ON DELETE CASCADE,
    razorpay_order_id VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    amount DECIMAL(10, 2) NOT NULL,
    refunded_amount DECIMAL(10, 2) DEFAULT 0.00
);

CREATE TABLE payouts (
    id SERIAL PRIMARY KEY,
    partner_id INT REFERENCES partners(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL
);

CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    booking_id INT REFERENCES bookings(id) ON DELETE CASCADE,
    customer_id INT REFERENCES users(id) ON DELETE CASCADE,
    partner_id INT REFERENCES partners(id) ON DELETE CASCADE,
    rating INT CHECK (rating >= 1 AND rating <= 5) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Proper indexes for partner search and booking time lookups
CREATE INDEX idx_partners_area ON partners(area);
CREATE INDEX idx_partners_type_status ON partners(type, status);
CREATE INDEX idx_partners_rating ON partners(rating_avg DESC);

CREATE INDEX idx_bookings_partner_time ON bookings(partner_id, start_at, end_at);
CREATE INDEX idx_bookings_customer ON bookings(customer_id);

CREATE INDEX idx_services_partner ON services(partner_id);
CREATE INDEX idx_services_category ON services(category);
