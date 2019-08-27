defmodule ServerWeb.SessionServer do
    use GenServer
     
    # TODO: The user should complete connection setup in a few minutes.
    # Cleanup channel ids that didn't updated in a few minutes.
    
    def start_link(_) do
        GenServer.start_link(__MODULE__, %{}, name: __MODULE__)
    end

    def init(state) do
        :ets.new(:sessions_table, [:set, :protected, :named_table])
        {:ok, state}
    end

    def get(key) do
        GenServer.call(__MODULE__, {:get, key})
    end

    def insert(key, value) do
        GenServer.call(__MODULE__, {:insert, key, value})
    end

    def update_counter(key) do
        GenServer.call(__MODULE__, {:update_counter, key})
    end

    def delete(key) do
        GenServer.call(__MODULE__, {:delete, key})
    end

    def handle_call({:get, key}, _from, state) do
        {:reply, :ets.lookup(:sessions_table, key), state}
    end

    def handle_call({:insert, key, value}, _from, state) do
        {:reply, :ets.insert_new(:sessions_table, {key, value}), state}
    end

    def handle_call({:update_counter, key}, _from, state) do
        {:reply, :ets.update_counter(:sessions_table, key, 1, {1, 0})}
    end

    def handle_call({:delete, key}, _from, state) do
        {:reply, :ets.delete(:sessions_table, key), state}
    end
end