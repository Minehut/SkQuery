package com.w00tmast3r.skquery.elements.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.w00tmast3r.skquery.SkQuery;
import com.w00tmast3r.skquery.api.Patterns;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;


@Patterns("async text from [url] %string%")
public class ExprURLTextAsync extends SimplePropertyExpression<String, String> {

    public static String response = null;

    @Override
    protected String getPropertyName() {
        return "URL";
    }

    @SuppressWarnings("resource")
	@Override
    public String convert(String s) {
        runAsyncRequest(s);
        return response;

    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }


    public void runAsyncRequest(String rawURLString){
        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(rawURLString);
                    Scanner a = new Scanner(url.openStream());
                    String str = "";
                    boolean first = true;
                    while(a.hasNext()){
                        if(first) str = a.next();
                        else str += " " + a.next();
                        first = false;
                    }
                    response = str;
                } catch(IOException ex) {
                    response = null;
                }
            }
        }.runTaskAsynchronously(SkQuery.getInstance());
    }
}
