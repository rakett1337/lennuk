<script>
  import { onMount } from "svelte";
  import { fade } from "svelte/transition";

  import { formatDateEU } from "../utils/dateUtils.js";

  let { flights = [], onSelect } = $props();
  let filteredFlights = $state([]);
  let filters = $state({
    origin: "",
    destination: "",
    maxPrice: "",
    departureDate: "",
  });
  let sortOrder = $state("asc");
  let origins = $state([]);
  let destinations = $state([]);
  let availableDates = $state([]);
  let loading = $state(true);

  onMount(() => {
    if (flights.length > 0) {
      processFlightData();
    }
  });

  function processFlightData() {
    origins = [
      ...new Map(
        flights.map((flight) => [
          flight.origin,
          { code: flight.origin, name: flight.originDetailedName },
        ]),
      ).values(),
    ];
    destinations = [
      ...new Map(
        flights.map((flight) => [
          flight.destination,
          { code: flight.destination, name: flight.destinationDetailedName },
        ]),
      ).values(),
    ];

    filteredFlights = [...flights];
    sortFlights();
    loading = false;
  }

  function sortFlights() {
    filteredFlights = filteredFlights.sort((a, b) => {
      return sortOrder === "asc"
        ? a.basePrice - b.basePrice
        : b.basePrice - a.basePrice;
    });
  }

  function toggleSortOrder() {
    sortOrder = sortOrder === "asc" ? "desc" : "asc";
  }

  function clearFilters() {
    filters = {
      origin: "",
      destination: "",
      maxPrice: "",
      departureDate: "",
    };
  }

  function handleMaxPriceInput(e) {
    const input = e.target;
    input.value = input.value.replace(/\D/g, "");
    filters.maxPrice = input.value;
  }

  $effect(() => {
    if (!loading && flights.length > 0) {
      const filtered = flights.filter((flight) => {
        const matchesOrigin =
          !filters.origin || flight.origin === filters.origin;
        const matchesDestination =
          !filters.destination || flight.destination === filters.destination;
        const matchesPrice =
          !filters.maxPrice || flight.basePrice <= parseFloat(filters.maxPrice);
        const matchesDate =
          !filters.departureDate ||
          flight.departureDate === filters.departureDate;
        return (
          matchesOrigin && matchesDestination && matchesPrice && matchesDate
        );
      });

      filteredFlights = filtered.sort((a, b) => {
        return sortOrder === "asc"
          ? a.basePrice - b.basePrice
          : b.basePrice - a.basePrice;
      });
    }
  });
</script>

<div class="flight-list">
  <h2>Flights from London</h2>
  {#if loading}
    <div class="loading">Loading flights...</div>
  {:else}
    <div class="filters">
      <div class="filter">
        <label for="origin">From</label>
        <select id="origin" bind:value={filters.origin}>
          <option value="">All airports</option>
          {#each origins as origin}
            <option value={origin.code}>{origin.code} - {origin.name}</option>
          {/each}
        </select>
      </div>
      <div class="filter">
        <label for="destination">To</label>
        <select id="destination" bind:value={filters.destination}>
          <option value="">All destinations</option>
          {#each destinations as destination}
            <option value={destination.code}>
              {destination.code} - {destination.name}
            </option>
          {/each}
        </select>
      </div>
      <div class="filter">
        <label for="departureDate">Departure Date</label>
        <input
          type="date"
          id="departureDate"
          bind:value={filters.departureDate}
          list="available-dates"
        />
        <datalist id="available-dates">
          {#each availableDates as date}
            <option value={date}></option>
          {/each}
        </datalist>
      </div>
      <div class="filter">
        <label for="maxPrice">Max Price (£)</label>
        <input
          id="maxPrice"
          type="text"
          bind:value={filters.maxPrice}
          placeholder="Any price"
          oninput={handleMaxPriceInput}
        />
      </div>
      <div class="filter">
        <label for="sortOrder">Order by</label>
        <button id="sortOrder" onclick={toggleSortOrder} class="sort-button">
          Price: {sortOrder === "asc" ? "Low to High ↑" : "High to Low ↓"}
        </button>
      </div>
      <div class="filter clear-filter">
        <!-- svelte-ignore a11y_label_has_associated_control -->
        <label>&nbsp;</label>
        <!-- Empty label for alignment -->
        <button onclick={clearFilters} class="clear-filters-button">
          Clear Filters
        </button>
      </div>
    </div>
    <div
      class="flights"
      in:fade|global={{
        delay: 200,
        duration: 200,
      }}
    >
      {#each filteredFlights as flight}
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <!-- svelte-ignore a11y_click_events_have_key_events -->
        <div class="flight-card">
          <div class="flight-header">
            <div class="route">{flight.origin} → {flight.destination}</div>
            <div class="price">£{flight.basePrice.toFixed(2)}</div>
          </div>
          <div class="flight-details">
            <div class="airports">
              <span>{flight.originDetailedName}</span>
              to
              <span>{flight.destinationDetailedName}</span>
            </div>
            <div class="dates">
              <div>Depart: {formatDateEU(flight.departureDate)}</div>
              <div>Return: {formatDateEU(flight.returnDate)}</div>
            </div>
          </div>
          <button onclick={() => onSelect(flight)}>Select</button>
        </div>
      {/each}
      {#if filteredFlights.length === 0}
        <div class="no-results">No flights match your criteria</div>
      {/if}
    </div>
  {/if}
</div>

<style>
  .filters .filter select,
  .filters .filter input,
  .filters .filter button {
    width: 100%;
    box-sizing: border-box;
  }

  .flight-list {
    margin-bottom: 30px;
  }
  .loading {
    text-align: center;
    padding: 30px;
    background: #3a3a3a;
    border-radius: 8px;
    margin-bottom: 20px;
  }
  .filters {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 20px;
    background: #3a3a3a;
    padding: 15px;
    border-radius: 8px;
  }
  .filter {
    display: flex;
    flex-direction: column;
    flex: 1 1 200px;
  }
  select,
  input {
    padding: 8px;
    border: 1px solid #555;
    border-radius: 4px;
    font-size: 16px;
    background: #2c2c2c;
    color: #f0f0f0;
  }
  .flights {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }
  .flight-card {
    background: #3a3a3a;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 15px;
    transition: transform 0.2s;
    border: 1px solid #555;
  }
  .flight-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }
  .flight-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
  }
  .route {
    font-weight: bold;
    font-size: 1.2em;
  }
  .price {
    color: #00ff00;
    font-weight: bold;
  }
  .flight-details {
    margin-bottom: 15px;
  }
  .airports {
    margin-bottom: 10px;
  }
  button {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  .no-results {
    grid-column: 1 / -1;
    text-align: center;
    padding: 30px;
    background: #3a3a3a;
    border-radius: 8px;
  }

  .sort-button {
    padding: 8px;
    border: 1px solid #555;
    border-radius: 4px;
    font-size: 16px;
    background: #2c2c2c;
    color: #f0f0f0;
    cursor: pointer;
    transition: background-color 0.2s;
    width: 100%;
  }

  .sort-button:hover {
    background: #444;
  }

  .clear-filters-button {
    background: #ff4444;
    color: white;
  }
  .clear-filters-button:hover {
    background: #cc0000;
  }

  @media (max-width: 600px) {
    .filter {
      flex: 1 1 100%;
    }
  }
</style>
