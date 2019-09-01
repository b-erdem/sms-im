defmodule ServerWeb.RoomChannel do
    use Phoenix.Channel
    alias ServerWeb.SessionServer
    alias ServerWeb.Presence

    def join("room:" <> room_id, params, socket) do
        # At most 2 subscribers allowed for one channel.
        # One for web, and one for mobile.
        # This can be changed in the future.
        # Maybe some users would want messaging from their computers
        # and their iPads at the same time.
        # In this case we should show all subscribers in the mobile app. 
        # send(self(), {:after_join, params})
        # {:ok, socket}
        send(self(), {:after_join, params})
        {:ok, assign(socket, :device, room_id <> " " <> params["device"])}

        # case SessionServer.get(room_id) do
        #     [] ->
        #         {:error, %{reason: "authorized"}}
        #     [{channel_id, counter} | _] when counter < 2 ->
        #         case SessionServer.update_counter(channel_id) do
        #             x when x < 3 ->
        #                 send(self(), {:after_join, params})
        #                 {:ok, socket}
        #             _ ->
        #                 {:error, %{reason: "unauthorized"}}
        #         end
        #     [{channel_id, _} | _] ->
        #         {:error, %{reason: "unauthorized"}}
        # end
    end

    # Join with Phoenix.Token disable temporarily.
    # def join("room:" <> room_id, params, socket) do
    #     secret = Application.get_env(:server, ServerWeb.Endpoint)[:secret_key_base]      
    #     token = Map.get(params, "token")
    #     case Phoenix.Token.verify(ServerWeb.Endpoint, secret, token, max_age: 86400) do
    #         {:ok, rid} ->
    #             # We should check if room_id is equal to channel_id that's encoded in token.
    #             # This prevents using other tokens to joining other channels.
    #             if rid == room_id do
    #                 # At most 2 subscribers allowed for one channel.
    #                 # One for web, and one for mobile.
    #                 # This can be changed in the future.
    #                 # Maybe some users would want messaging from their computers
    #                 # and their iPads at the same time.
    #                 # In this case we should show all subscribers in the mobile app. 
    #                 case SessionServer.get(rid) do
    #                     [] ->
    #                         :error
    #                     [{channel_id, counter} | _] when counter < 2 ->
    #                         case SessionServer.update_counter(channel_id) do
    #                             x when x < 3 ->
    #                                 {:ok, socket}
    #                             _ ->
    #                                 :error
    #                         end
    #                     [{channel_id, _} | _] ->
    #                         :error
    #                 end
    #             else
    #                 {:error, "unauthorized"}
    #             end
    #         {:error, reason} ->
    #             :error
    #     end
    # end

    def handle_info({:after_join, params}, socket) do
        broadcast_from!(socket, "user_entered", params)
        push(socket, "presence_state", Presence.list(socket))
        {:ok, _} = Presence.track(socket, socket.assigns.device, %{
          online_at: inspect(System.system_time(:second))
        })
        {:noreply, socket}
      end

    def handle_in("new_msg", %{"body" => body, "from" => from, "timestamp" => timestamp}, socket) do
        broadcast!(socket, "new_msg", %{body: body, from: from, timestamp: timestamp})
        {:noreply, socket}
    end

    def handle_in("send_sms", payload, socket) do
        broadcast!(socket, "send_sms", payload)
        {:reply, :ok, socket}
    end

    def handle_in("last_10_messages", payload, socket) do
        broadcast_from!(socket, "last_10_messages", payload)
        {:reply, :ok, socket}
    end
end