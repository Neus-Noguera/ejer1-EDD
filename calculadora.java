import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculadora: SOLO suma, resta, multiplicación y potencia.
 * Entradas: enteros en el rango [-99999 ... 99999].
 * Servidor: http://localhost:8080/calcular?a=2&b=10&op=potencia
 * Operaciones (op): suma | resta | multiplicacion | potencia
 */
public class Calculadora {

    static final int MIN = -99999;
    static final int MAX = 99999;

    /** Valida que el texto sea un entero dentro del rango permitido. */
    static int validar(String texto, String nombre) {
        if (texto == null || !texto.matches("-?\\d{1,5}")) {
            throw new IllegalArgumentException(nombre + " debe ser un entero de hasta 5 dígitos");
        }
        int n = Integer.parseInt(texto);
        if (n < MIN || n > MAX) {
            throw new IllegalArgumentException(nombre + " fuera de rango [-99999, 99999]");
        }
        return n;
    }

    static BigInteger calcular(int a, int b, String op) {
        BigInteger x = BigInteger.valueOf(a), y = BigInteger.valueOf(b);
        switch (op) {
            case "suma":           return x.add(y);
            case "resta":          return x.subtract(y);
            case "multiplicacion": return x.multiply(y);
            case "potencia":
                if (b < 0) throw new IllegalArgumentException("El exponente no puede ser negativo (resultado no entero)");
                return x.pow(b);
            default: throw new IllegalArgumentException("Operación no permitida: " + op);
        }
    }

    static Map<String, String> parseQuery(String q) {
        Map<String, String> m = new HashMap<>();
        if (q == null) return m;
        for (String par : q.split("&")) {
            String[] kv = par.split("=", 2);
            if (kv.length == 2) m.put(kv[0], kv[1]);
        }
        return m;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/calcular", ex -> {
            int codigo = 200;
            String cuerpo;
            try {
                Map<String, String> p = parseQuery(ex.getRequestURI().getRawQuery());
                int a = validar(p.get("a"), "a");
                int b = validar(p.get("b"), "b");
                cuerpo = "{\"resultado\":\"" + calcular(a, b, String.valueOf(p.get("op"))) + "\"}";
            } catch (IllegalArgumentException e) {
                codigo = 400;
                cuerpo = "{\"error\":\"" + e.getMessage() + "\"}";
            }
            byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(codigo, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        });
        server.start();
        System.out.println("Servidor en http://localhost:8080  (Ctrl+C para parar)");
    }
}
