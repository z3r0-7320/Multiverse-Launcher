package multiverse.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class NullOnFailTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        final TypeAdapter<T> originalAdapter = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                originalAdapter.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                try {
                    return originalAdapter.read(in);
                } catch (JsonSyntaxException e) {
                    in.skipValue();
                    return null;
                }
            }
        };
    }
}