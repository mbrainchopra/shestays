# SheStay Professional UI & Functionality Upgrade

## Included
- Shared responsive design system: `src/main/resources/static/css/shestay.css`
- Shared animation/interaction script: `src/main/resources/static/js/shestay.js`
- Redesigned public landing page, login, registration and user dashboard
- Redesigned property search/list/detail and booking pages
- Added missing `my-bookings.html`
- Redesigned owner dashboard, registration, properties, rooms and booking request pages
- Redesigned admin dashboard, users, owners, properties, rooms, owner properties and bookings pages
- Search now supports location + property type + maximum monthly rent
- Property detail only exposes ACTIVE properties
- Booking validates property/room relationship and availability
- Booking rejects past check-in dates
- Duplicate REQUESTED/APPROVED booking requests for the same user and room are prevented
- Owner approval rechecks ownership, request status, check-in date and room availability
- Admin user statistics count actual USER accounts rather than every user row

## Run
1. Create MySQL database `shestay`.
2. Check `src/main/resources/application.properties` and update the MySQL password if needed.
3. Open the project in IntelliJ IDEA.
4. Run `ShestayApplication`.
5. Open `http://localhost:8080/`.

## Important
The application still uses plain-text passwords because the project intentionally keeps a simple Controller -> Repository -> Entity architecture. For a real production deployment, replace this with BCrypt/Spring Security and CSRF protection.

The local environment used for this upgrade could not download Maven dependencies, so a full Maven package/test run could not be completed here. Template route consistency and source-level structure were checked.
