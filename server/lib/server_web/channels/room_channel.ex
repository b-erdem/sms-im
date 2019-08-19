defmodule ServerWeb.RoomChannel do
    use Phoenix.Channel

    def join("room:lobby", _message, socket) do
        {:ok, socket}
    end

    def join("room:" <> _room_id, _params, socket) do
        {:ok, socket}
    end

    def handle_in("new_msg", %{"message" => message, "to" => to}, socket) do
        broadcast!(socket, "new_msg", %{message: message, to: to})
        {:noreply, socket}
    end

    def handle_in("contacts", %{"body" => body}, socket) do
        broadcast!(socket, "contacts", %{body: body})
        {:noreply, socket}
    end

    def handle_in("last_10_messages", payload, socket) do
        broadcast!(socket, "last_10_messages", payload)
        {:noreply, socket}
    end

    def handle_in("last_10", payload, socket) do
        broadcast!(socket, "last_10", payload)
        {:noreply, socket}
    end
end