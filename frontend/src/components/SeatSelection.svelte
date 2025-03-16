<script>
  import { onMount } from "svelte";
  import { fly } from "svelte/transition";

  let { seats = [], selectedFlight, numSeats, onBack, onComplete } = $props();

  let selectedSeatIndices = $state([]);
  let recommendedSeatIndices = $state([]);

  onMount(() => {
    recommendedSeatIndices = seats
      .map((seat, index) => ({ seat, index }))
      .filter((item) => item.seat.recommended)
      .map((item) => item.index);
  });

  function toggleSeat(index) {
    const seat = seats[index];
    if (seat.booked) return;

    if (selectedSeatIndices.includes(index)) {
      // If already selected, just deselect it
      selectedSeatIndices = selectedSeatIndices.filter((i) => i !== index);
    } else {
      if (selectedSeatIndices.length < numSeats) {
        // If we haven't reached max seats, add it
        selectedSeatIndices = [...selectedSeatIndices, index];
      } else {
        // If we've reached max seats, replace the earliest selected seat
        const newSelection = [...selectedSeatIndices];
        newSelection.shift();
        newSelection.push(index);
        selectedSeatIndices = newSelection;
      }
    }
  }

  function getSeatStatus(index) {
    const seat = seats[index];
    if (seat.booked) return "booked";
    if (selectedSeatIndices.includes(index)) return "selected";
    if (recommendedSeatIndices.includes(index)) return "recommended";
    return "";
  }
</script>

<div class="seat-selection" in:fly={{ duration: 200, x: 600 }}>
  <h2>Select Your Seats</h2>
  <p>
    Flight: <b>{selectedFlight.originDetailedName}</b>
    to
    <b>{selectedFlight.destinationDetailedName}</b>
  </p>

  <div class="legend">
    <div class="legend-item">
      <div class="seat-sample"></div>
      <span>Available</span>
    </div>
    <div class="legend-item">
      <div class="seat-sample recommended"></div>
      <span>Recommended</span>
    </div>
    <div class="legend-item">
      <div class="seat-sample selected"></div>
      <span>Selected</span>
    </div>
    <div class="legend-item">
      <div class="seat-sample booked"></div>
      <span>Booked</span>
    </div>
  </div>

  <div class="airplane">
    <div class="cabin">
      {#each Array(Math.ceil(seats.length / 6)) as _, row}
        <div class="row">
          <div class="row-number">{row + 1}</div>

          {#each [0, 1, 2] as col}
            {@const seatIndex = row * 6 + col}
            {#if seatIndex < seats.length}
              <!-- svelte-ignore a11y_click_events_have_key_events -->
              <!-- svelte-ignore a11y_no_static_element_interactions -->
              <div
                class="seat {getSeatStatus(seatIndex)}"
                onclick={() => toggleSeat(seatIndex)}
                title={seats[seatIndex].seatNumber}
              >
                {seats[seatIndex].seatNumber}
              </div>
            {/if}
          {/each}

          <div class="aisle"></div>

          {#each [3, 4, 5] as col}
            {@const seatIndex = row * 6 + col}
            {#if seatIndex < seats.length}
              <!-- svelte-ignore a11y_click_events_have_key_events -->
              <!-- svelte-ignore a11y_no_static_element_interactions -->
              <div
                class="seat {getSeatStatus(seatIndex)}"
                onclick={() => toggleSeat(seatIndex)}
                title={seats[seatIndex].seatNumber}
              >
                {seats[seatIndex].seatNumber}
              </div>
            {/if}
          {/each}
        </div>
      {/each}
    </div>
  </div>

  <div class="selected-seats">
    <h3>Your selection:</h3>
    {#if selectedSeatIndices.length > 0}
      <ul>
        {#each selectedSeatIndices as index}
          <li>
            Seat {seats[index].seatNumber}
            {#if seats[index].window}
              (Window)
            {/if}
            {#if seats[index].aisle}
              (Aisle)
            {/if}
            {#if seats[index].exitRow}
              (Exit Row)
            {/if}
            {#if seats[index].extraLegroom}
              (Extra Legroom)
            {/if}
          </li>
        {/each}
      </ul>
    {:else}
      <p>No seats selected</p>
    {/if}
  </div>

  <div class="actions">
    <button onclick={onBack}>Back</button>
    <button
      onclick={onComplete}
      class="primary"
      disabled={selectedSeatIndices.length !== numSeats}
    >
      Complete Booking
    </button>
  </div>
</div>

<style>
  .seat-selection {
    background: #3a3a3a;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  .airplane {
    margin: 30px 0;
    display: flex;
    justify-content: center;
  }
  .cabin {
    background: #2c2c2c;
    padding: 20px;
    border-radius: 10px;
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1);
  }
  .row {
    display: flex;
    margin-bottom: 10px;
    align-items: center;
  }
  .row-number {
    width: 20px;
    text-align: center;
    margin-right: 10px;
    font-weight: bold;
  }
  .seat {
    width: 40px;
    height: 40px;
    margin: 0 5px;
    background: #555;
    border-radius: 5px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.2s;
    box-sizing: border-box;
    border: 2px solid transparent;
  }
  .seat:hover:not(.booked) {
    transform: scale(1.1);
  }
  .seat.recommended {
    background: #555;
    border: 2px solid #1db825;
  }
  .seat.selected {
    background: #1d70b8;
    color: white;
  }
  .seat.booked {
    background: #d4351c;
    color: white;
    cursor: not-allowed;
    opacity: 0.7;
  }
  .aisle {
    width: 20px;
  }
  .legend {
    display: grid;
    grid-template-columns: repeat(2, auto);
    gap: 15px 30px;
    margin: 0 auto 20px auto;
    width: fit-content;
    justify-content: center;
  }
  .legend-item {
    display: flex;
    align-items: center;
    white-space: nowrap;
  }
  .seat-sample {
    width: 20px;
    height: 20px;
    background: #555;
    border-radius: 3px;
    margin-right: 5px;
  }
  .seat-sample.recommended {
    background: #555;
    border: 2px solid #1db825;
  }
  .seat-sample.selected {
    background: #1d70b8;
  }
  .seat-sample.booked {
    background: #d4351c;
    opacity: 0.7;
  }
  .selected-seats {
    margin: 20px 0;
  }
  .actions {
    display: flex;
    justify-content: space-between;
  }
  button {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  button.primary {
    background: #49b81d;
    color: white;
  }
  button.primary:disabled {
    background: #cccccc;
    cursor: not-allowed;
  }
</style>
