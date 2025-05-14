export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest extends LoginRequest {
  email?: string
}

export interface AuthResponse {
  token: string
  username: string
} 