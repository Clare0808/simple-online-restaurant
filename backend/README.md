# Java login backend

The backend exposes local sign-up/login endpoints plus cart and purchase-history endpoints. Accounts use MongoDB collection `logins`; cart entries use `orders`; completed purchases use `buys`. Passwords are stored as BCrypt hashes. Google login is not included.

Requirements: Java 17+, Maven, and a running MongoDB instance.

Start the Java backend from the project root with `npm run backend`, or from this directory with Maven:

```powershell
$env:MONGO_URL = "mongodb://127.0.0.1:27017/simple_online_restaurant"
$env:JWT_SECRET = "set-a-long-random-secret-for-local-development"
mvn spring-boot:run
```

`npm run backend` loads variables from `backend/.env` and can use the local Java/Maven tools in the system temporary directory if they are present.

The backend listens on port `3001` by default so it can run beside the reference project on port `3000`. The local launcher converts a Docker-only MongoDB hostname `mongo` to `127.0.0.1`. The Vue CLI development server proxies `/api` to `http://localhost:3001`. Run the frontend separately with `npm run serve` from the project root.
