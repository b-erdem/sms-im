defmodule ServerWeb.AuthController do
    use ServerWeb, :controller
    alias ServerWeb.SessionServer

    def generate_otp(conn, _params) do
        secret_key_base = Application.get_env(:server, ServerWeb.Endpoint)[:secret_key_base]
        otp_secret = :crypto.strong_rand_bytes(16) |> Base.encode32
        otp_code = :pot.totp(otp_secret, [interval_length: 120])

        channel_id = :crypto.strong_rand_bytes(16) |> Base.encode32
        auth_token = Phoenix.Token.sign(ServerWeb.Endpoint, secret_key_base, channel_id)
        
        case SessionServer.insert(otp_code, {auth_token, channel_id, otp_secret}) do
            true ->
                json(conn, %{code: otp_code, token: auth_token, channel_id: channel_id})
            _ ->
                json(conn, %{message: "An error occurred. Try again later."})
        end
    end

    def generate_qr_code(conn, _params) do
        secret_key_base = Application.get_env(:server, ServerWeb.Endpoint)[:secret_key_base]
        secret = :crypto.strong_rand_bytes(31)
        channel_id = :crypto.hash(:sha256, secret) |> Base.encode16
        auth_token = Phoenix.Token.sign(ServerWeb.Endpoint, secret_key_base, channel_id)
        qr_code_svg = channel_id
        |> EQRCode.encode
        |> EQRCode.svg
        
        case SessionServer.insert(channel_id, 0) do
            true ->
                json(conn, %{channel_id: channel_id, qr_code_svg: qr_code_svg, token: auth_token})

            # Suspicious activity. 🤔 🤔
            _ ->
                json(conn, %{message: "An error occurred. Try again later."})
        end
    end

    def authenticate(conn, params) do
        code = Map.get(params, "code")
        if code == nil do
            json(conn, %{message: "no otp code provided."})
        end

        case SessionServer.get(code) do
            [] ->
                json(conn, %{message: "invalid otp."})
            [{otp_code, {token, channel_id, otp_secret}}] ->
                case :pot.valid_totp(otp_code, otp_secret, [interval_length: 120]) do
                    true ->
                        SessionServer.delete(code)
                        json(conn, %{token: token, channel_id: channel_id})
                    _ ->
                        SessionServer.delete(code)
                        json(conn, %{message: "invalid otp."})
                end
        end
    end
end