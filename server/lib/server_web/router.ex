defmodule ServerWeb.Router do
  use ServerWeb, :router

  pipeline :api do
    plug :accepts, ["json"]
  end

  scope "/api", ServerWeb do
    pipe_through :api
    get "/auth/generate_qr_code", AuthController, :generate_qr_code
  end
end
