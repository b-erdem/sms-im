defmodule ServerWeb.RoomChannel do
    use Phoenix.Channel
    alias ServerWeb.SessionServer

    def join("room:" <> room_id, _params, socket) do
        # At most 2 subscribers allowed for one channel.
        # One for web, and one for mobile.
        # This can be changed in the future.
        # Maybe some users would want messaging from their computers
        # and their iPads at the same time.
        # In this case we should show all subscribers in the mobile app. 
        case SessionServer.get(room_id) do
            [] ->
                :error
            [{channel_id, counter} | _] when counter < 2 ->
                case SessionServer.update_counter(channel_id) do
                    x when x < 3 ->
                        {:ok, socket}
                    _ ->
                        :error
                end
            [{channel_id, _} | _] ->
                :error
        end
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

    def handle_in("new_msg", %{"message" => message, "to" => to}, socket) do
        broadcast!(socket, "new_msg", %{message: message, to: to})
        {:noreply, socket}
    end

    def handle_in("send_sms", payload, socket) do
        broadcast!(socket, "send_sms", payload)
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