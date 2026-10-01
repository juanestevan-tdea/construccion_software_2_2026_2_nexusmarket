package com.nexusmarket.domain.ports.in;

import com.nexusmarket.adapters.rest.dtos.requests.LoginRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.RegisterRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.AuthResponseDTO;

public interface AuthenticationUseCasePort {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login(LoginRequestDTO request);
}
