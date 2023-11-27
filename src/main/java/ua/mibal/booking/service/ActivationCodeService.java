/*
 * Copyright (c) 2023. Mykhailo Balakhon, mailto:9mohapx9@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ua.mibal.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.mibal.booking.model.entity.ActivationCode;
import ua.mibal.booking.model.entity.User;
import ua.mibal.booking.repository.ActivationCodeRepository;
import ua.mibal.booking.service.security.CodeGenerationService;

import java.util.function.Consumer;

/**
 * @author Mykhailo Balakhon
 * @link <a href="mailto:9mohapx9@gmail.com">email</a>
 */
@RequiredArgsConstructor
@Service
public class ActivationCodeService {
    private final ActivationCodeRepository activationCodeRepository;
    private final CodeGenerationService codeGenerationService;

    @Transactional
    public void activateByCode(String activationCode) {
        performIfPresentByCode(activationCode, code -> {
            code.getUser().setEnabled(true);
            activationCodeRepository.delete(code);
        });
    }

    @Transactional
    public void changePasswordByCode(String activationCode, String password) {
        performIfPresentByCode(activationCode, code -> {
            code.getUser().setPassword(password);
            activationCodeRepository.delete(code);
        });
    }

    private void performIfPresentByCode(String activationCode, Consumer<ActivationCode> action) {
        activationCodeRepository
                .findByCodeFetchUser(activationCode)
                .ifPresent(action);
    }

    public ActivationCode generateAndSaveCodeForUser(User user) {
        String code = codeGenerationService.generateCode();
        ActivationCode activationCode = ActivationCode.builder()
                .user(user)
                .code(code)
                .build();
        return activationCodeRepository.save(activationCode);
    }
}
