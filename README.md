# Lennuk

Demo Flight Booking App. Built with [Spring Boot](https://spring.io/projects/spring-boot) and [Svelte 5](https://svelte.dev/docs/svelte/overview).

## Quickstart

### Live Version

The app is live at [murd.ee](https://murd.ee).

### Running Locally

**Note:** The app uses the [Amadeus API](https://developers.amadeus.com/) to fetch flights. If the app fails to retrieve flights for any reason (e.g., missing API keys), it will create 4 sample flights. The API is quite limited on the free tier, and for demonstration purposes, we pretend to be from the UK.

#### Requirements

- [Docker](https://docs.docker.com/get-started/get-docker/)

#### Steps

1. Copy the environment example file:

   ```bash
   cp .envexample .env
   ```

2. Build and run the app using Docker Compose:

   ```bash
   docker compose up -d --build
   ```

3. Visit [http://localhost:8000](http://localhost:8000).

#### To Stop the App

```bash
docker compose down
```

## Notes

- All planes have a fixed 3-3 seating layout with six seats per row.
- Only round-trip flights.
- Seats are randomly generated and about 30% of them get assigned booked status
- Flights are exclusively departing from London.
- Each user can purchase a maximum of two tickets per flight.
- Opinionated seat recommendation system using weighted scoring.
- No tests
