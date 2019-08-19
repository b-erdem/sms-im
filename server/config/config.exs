# This file is responsible for configuring your application
# and its dependencies with the aid of the Mix.Config module.
#
# This configuration file is loaded before any dependency and
# is restricted to this project.

# General application configuration
use Mix.Config

# Configures the endpoint
config :server, ServerWeb.Endpoint,
  url: [host: "localhost"],
  secret_key_base: "wSq37Lcme4MITb7lkmVlYlnGdTgDRlP3cyuZlkd+0UjCoxaZ3mbJDM4D6TbvuBEK",
  render_errors: [view: ServerWeb.ErrorView, accepts: ~w(json)],
  pubsub: [name: Server.PubSub, adapter: Phoenix.PubSub.PG2]

# Configures Elixir's Logger
config :logger, :console,
  format: "$time $metadata[$level] $message\n",
  metadata: [:request_id]

# Use Jason for JSON parsing in Phoenix
config :phoenix, :json_library, Jason

# Import environment specific config. This must remain at the bottom
# of this file so it overrides the configuration defined above.
import_config "#{Mix.env()}.exs"
