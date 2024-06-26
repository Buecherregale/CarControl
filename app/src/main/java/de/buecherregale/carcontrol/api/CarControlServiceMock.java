package de.buecherregale.carcontrol.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import retrofit2.Response;

public class CarControlServiceMock implements CarControlService {
    @Nullable
    @Override
    public Object postMotor(@NonNull Motor motor, @NonNull Continuation<? super PostResponse> $completion) {
        return new PostResponse("1000");
    }

    @Nullable
    @Override
    public Object postServo(@NonNull Servo servo, @NonNull Continuation<? super PostResponse> $completion) {
        return new PostResponse("1000");
    }

    @Nullable
    @Override
    public Object getConstants(@NonNull Continuation<? super Constants> $completion) {
        return new Constants(1100, 400, 1100, 400);
    }

    @Nullable
    @Override
    public Object activateLKA(@NonNull Continuation<? super Response<Unit>> $completion) {
        return null;
    }
}
