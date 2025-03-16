<script>
  import { onMount } from "svelte";
  import { fly } from "svelte/transition";

  import { formatDateEU } from "./utils/dateUtils.js";
  import FlightList from "./components/FlightList.svelte";
  import SeatSelection from "./components/SeatSelection.svelte";

  let flights = $state([]);
  let selectedFlight = $state(null);
  let numSeats = $state(1);
  let preferences = $state({
    windowSeat: false,
    extraLegroom: false,
    exitRowProximity: false,
    seatsTogetherRequired: true,
  });
  let seats = $state([]);
  let step = $state("flights"); // 'flights', 'preferences' or 'seats'
  let loading = $state(true);

  onMount(async () => {
    try {
      const response = await fetch("/api/flights");
      flights = await response.json();
    } catch (error) {
      console.error("Error fetching flights:", error);
    } finally {
      loading = false;
    }
  });

  function selectFlight(flight) {
    selectedFlight = flight;
    step = "preferences";
  }

  async function findSeats() {
    try {
      loading = true;
      const params = new URLSearchParams({
        windowSeat: preferences.windowSeat.toString(),
        extraLegroom: preferences.extraLegroom.toString(),
        exitRowProximity: preferences.exitRowProximity.toString(),
        numSeats: numSeats.toString(),
        seatsTogetherRequired: preferences.seatsTogetherRequired.toString(),
      });

      const response = await fetch(
        `/api/flights/${selectedFlight.id}/seats?${params}`,
      );
      const result = await response.json();
      seats = result.data;
      step = "seats";
    } catch (error) {
      console.error("Error fetching seats:", error);
    } finally {
      loading = false;
    }
  }

  function resetSelection() {
    step = "flights";
    selectedFlight = null;
  }
</script>

<main>
  <header>
    <div class="logo">
      <img
        src="https://upload.wikimedia.org/wikipedia/commons/8/83/Flag_of_the_United_Kingdom_%283-5%29.svg"
        alt="UK Flag"
      />
      <h1>British Airways Demo</h1>
    </div>
  </header>

  {#if loading}
    <div class="loading">
      <div class="spinner"></div>
      <p>Loading...</p>
    </div>
  {:else if step === "flights"}
    <FlightList {flights} onSelect={selectFlight} />
  {:else if step === "preferences"}
    <div class="preferences" in:fly={{ duration: 200, x: 600 }}>
      <h2>
        Flight from {selectedFlight.originDetailedName} to {selectedFlight.destinationDetailedName}
      </h2>
      <p>
        Departure: {formatDateEU(selectedFlight.departureDate)} | Return: {formatDateEU(
          selectedFlight.returnDate,
        )}
      </p>

      <div class="form-group">
        <label>
          Number of seats:
          <select bind:value={numSeats}>
            <option value={1}>1 seat</option>
            <option value={2}>2 seats</option>
          </select>
        </label>
      </div>

      <div class="form-group">
        <label>
          <input type="checkbox" bind:checked={preferences.windowSeat} />
          Window seat
        </label>
      </div>

      <div class="form-group">
        <label>
          <input type="checkbox" bind:checked={preferences.extraLegroom} />
          Extra legroom
        </label>
      </div>

      <div class="form-group">
        <label>
          <input type="checkbox" bind:checked={preferences.exitRowProximity} />
          Near exit row
        </label>
      </div>

      <div class="form-group">
        <label>
          <input
            type="checkbox"
            bind:checked={preferences.seatsTogetherRequired}
          />
          Seats together (if applicable)
        </label>
      </div>

      <div class="actions">
        <button onclick={resetSelection}>Back</button>
        <button onclick={findSeats} class="primary">Find Seats</button>
      </div>
    </div>
  {:else if step === "seats"}
    <SeatSelection
      {seats}
      {selectedFlight}
      {numSeats}
      onBack={() => (step = "preferences")}
      onComplete={() => alert("Booking completed!")}
    />
  {/if}
</main>

<style>
  main {
    max-width: 1000px;
    margin: 0 auto;
    padding: 20px;
    background: #2c2c2c;
    color: #f0f0f0;
  }
  header {
    margin-bottom: 30px;
  }
  .logo {
    display: flex;
    align-items: center;
  }
  .logo img {
    height: 40px;
    margin-right: 15px;
  }
  h1 {
    color: #f0f0f0;
  }
  .loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 50px;
    background: #3a3a3a;
    border-radius: 8px;
  }
  .spinner {
    border: 4px solid #f3f3f3;
    border-top: 4px solid #1d70b8;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin-bottom: 20px;
  }
  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
  button {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  .preferences {
    background: #3a3a3a;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 20px;
  }
  .preferences h2 {
    margin-bottom: 10px;
    color: #f0f0f0;
  }
  .preferences p {
    margin-bottom: 20px;
    color: #ccc;
  }
  .form-group {
    margin-bottom: 15px;
  }
  .form-group label {
    display: flex;
    align-items: center;
    color: #f0f0f0;
  }
  .form-group input[type="checkbox"] {
    margin-right: 10px;
  }
  .form-group select {
    margin-left: 10px;
    padding: 5px;
    border-radius: 4px;
    border: 1px solid #ccc;
    background: #2c2c2c;
    color: #f0f0f0;
  }
  .actions {
    display: flex;
    justify-content: space-between;
  }
</style>
