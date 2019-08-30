defmodule ServerWeb.AuthController do
    use ServerWeb, :controller
    alias ServerWeb.SessionServer

    def generate_qr_code(conn, _params) do
        secret_key_base = Application.get_env(:server, ServerWeb.Endpoint)[:secret_key_base]
        secret = :crypto.strong_rand_bytes(31)
        channel_id = :crypto.hash(:sha256, secret) |> Base.encode16
        auth_token = Phoenix.Token.sign(ServerWeb.Endpoint, secret_key_base, channel_id)
        qr_code_svg = channel_id
        |> EQRCode.encode()
        |> EQRCode.svg(width: 264)
        
        case SessionServer.insert(channel_id, 0) do
            true ->
                json(conn, %{channel_id: channel_id, qr_code_svg: qr_code_svg, token: auth_token})

            # Suspicious activity. 🤔 🤔
            _ ->
                json(conn, %{message: "An error occurred. Try again later."})
        end
    end
end