package com.wynd.vop.framework.rest.provider;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MessagesAwareResponseEntity<T extends ProviderResponse> extends ResponseEntity<T> {
    public MessagesAwareResponseEntity(T body, HttpStatus successStatus) {
        super(body, body.getMessages().stream()
                .filter(message -> !message.getHttpStatus().is2xxSuccessful())
                .findAny()
                .map(Message::getHttpStatus)
                .orElse(successStatus));
    }
}