delete from cart;
delete from product_order;
delete from order_address;
delete from product;
delete from category;
delete from users;

insert into category (category_name, category_image, is_active, created_at, updated_at)
values ('Shirts', 'shirts.png', true, current_timestamp, current_timestamp);

insert into users (
    name,
    mobile,
    email,
    address,
    city,
    state,
    pin_code,
    password,
    profile_image,
    role,
    is_enable,
    account_status_non_locked,
    accountfailed_attempt_count,
    account_lock_time,
    reset_tokens,
    created_at,
    updated_at
)
values (
           'Admin2',
           '073000000',
           'admin2@example.com',
           'Admin Street 1',
           'Skopje',
           'Centar',
           '1000',
           '$2b$12$1YyGR7F7ewFpEQnRbxJ/p.J3PM0ZYDPmYB.6A/QKWF0Mz.Ha/zRpK',
           'default.jpg',
           'ROLE_ADMIN',
           true,
           true,
           0,
           null,
           null,
           current_timestamp,
           current_timestamp
       );
insert into product (
    product_title,
    product_description,
    product_category,
    product_price,
    product_stock,
    product_image,
    discount,
    discount_price,
    is_active,
    created_at,
    updated_at
)
values (
           'White Shirt',
           'Seeded E2E product for automated tests.',
           'Shirts',
           10.99,
           10,
           'white-shirt.png',
           10,
           9.89,
           true,
           current_timestamp,
           current_timestamp
       );