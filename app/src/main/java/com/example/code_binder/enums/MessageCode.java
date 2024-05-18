package com.example.code_binder.enums;

public enum MessageCode {
    START_AUTH(0), // запуск процесса аутентификации устройства (ожидание логина и пароля или mac-адреса)
    RECONNECTION(1), // в случае если было потеряно соединение с устройством во время выполнения заявки
    SEND_PROPOSAL(2), // отправка необходимого списка продуктов из json-файла от сервера к клиентам
    ACCESS_GRANTED(3), // устройство прошло авторизацию
    ACCESS_DENIED(4), // устройство не прошло аутентификацию
    PROPOSAL_ACCEPTED(5), // клиент принял заявку от сервера
    PROPOSAL_REJECTED(6), // клиент отклонил заявку от сервера
    JOB_DONE(7); // клиент выполнил заявку (штрих-коды просканированы без ошибок)

    private final int code;

    MessageCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
