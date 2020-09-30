package com.pnet.routing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pnet.abstractions.Message;
import com.pnet.util.PersistentDataService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.io.IOException;

@Data
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true) //Для делегированных свойств
public class RoutingMessage implements Message {
    //TODO: Дублированные поля в JSON: this.fields; msg.fields
    // Унаследоваться от MessageImpl не получается, т.к. lombok requires no-args constructor in base class
    // https://stackoverflow.com/questions/29740078/how-to-call-super-constructor-in-lombok
    // ?объеденить с MessageImpl
    // Минусы:
    //  хотелось разделить имплементацию интерфейса Message и поля бизнес-логики (?актуально ли)
    // ?Убрать делегирование, оставить public-поле/getter
    //  Минусы: более длинное обращение к членам msg
    @Delegate
    private final MessageImpl msg;
    private final boolean isGreeting;
    private int reproducedCount;
    public static RoutingMessage fromMessage(Message message) throws IOException {
        try {
            return new RoutingMessage(
                    PersistentDataService.fromJson(MessageImpl.class, PersistentDataService.toJson(message)), false);
        } catch (IOException ioException) {
            throw ioException;
        }
    }
}
