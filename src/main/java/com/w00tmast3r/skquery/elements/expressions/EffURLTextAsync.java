package com.w00tmast3r.skquery.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.w00tmast3r.skquery.SkQuery;
import com.w00tmast3r.skquery.api.Patterns;
import org.bukkit.event.Event;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.*;


@Patterns("send async [get] request to %string%")
public class EffURLTextAsync extends Effect {


    static String LatestResponseBody;
    private static final Field DELAYED;
    private String RequestURL;

    static {
        Field _DELAYED = null;
        try {
            _DELAYED = Delay.class.getDeclaredField("delayed");
            _DELAYED.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Skript.warning("Skript's 'delayed' method could not be resolved. Some Skript warnings may not be available.");
        }
        DELAYED = _DELAYED;
    }

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Expression<String> url;


    @Override
    protected void execute(Event e) {
        RequestURL = url.getSingle(e);

        CompletableFuture.supplyAsync(() -> sendSimpleGetRequest(e), threadPool)
        .whenComplete((resp, err) -> {
            if (err != null) {
                err.printStackTrace();
                LatestResponseBody = null;
                return;
            }

            Bukkit.getScheduler().runTask(SkQuery.getInstance(), () -> {
                LatestResponseBody = resp;
                if (getNext() != null) {
                    TriggerItem.walk(getNext(), e);
                }
            });
        });
    }

    @Override
    protected TriggerItem walk(Event e) {
        debug(e, true);
        delay(e);
        execute(e);
        return null;
    }

    @SuppressWarnings("unchecked")
    private void delay(Event e) {
        if (DELAYED != null) {
            try {
                ((Set<Event>) DELAYED.get(null)).add(e);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private String sendSimpleGetRequest(Event e) {
        if(RequestURL == null){
            return null;
        }
        try (Scanner scanner = new Scanner(new URL(RequestURL).openStream(),
                StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "send a simple async http get request";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        url = (Expression<String>) exprs[0];
        return true;
    }

}
