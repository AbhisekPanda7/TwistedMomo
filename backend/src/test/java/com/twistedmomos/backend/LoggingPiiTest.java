package com.twistedmomos.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Guards the rule that no log statement may carry personal data.
 *
 * <p>Logs are shipped off the box and retained for weeks, so an email address or a
 * delivery address in a log line outlives the request that produced it and lands
 * somewhere with far weaker access control than the database. This scans the source
 * rather than runtime output because the point is to fail the build, not to notice
 * later that production has been leaking.
 */
class LoggingPiiTest {

    private static final Path SOURCE_ROOT = Path.of("src/main/java");

    /**
     * Accessors that return personal data, and the credential accessors alongside them.
     *
     * <p>{@code getName()} is deliberately absent: it is ambiguous. On a User it is
     * personal data, on a Role or an enum it is a harmless label. It is covered by
     * {@link #PERSONAL_NAME_ACCESS} instead, which matches on the receiver.
     */
    private static final List<String> FORBIDDEN_ACCESSORS = List.of(
            "getEmail()",
            "getPhone()",
            "getRecipientName()",
            "getAddressLine1()",
            "getAddressLine2()",
            "getPostalCode()",
            "getNotes()",
            "getPassword()",
            "getToken()");

    /**
     * {@code getName()} only when the receiver is person-shaped — a variable named for a
     * person, or a getter returning one. Anchored so {@code customerRole.getName()} and
     * {@code user.getRole().getName()} do not match: those return an enum label.
     */
    private static final Pattern PERSONAL_NAME_ACCESS = Pattern.compile(
            "(?:\\b(?:user|customer|recipient|owner)|get(?:User|Customer|Recipient|Owner)\\(\\))\\s*\\.getName\\(\\)",
            Pattern.CASE_INSENSITIVE);

    /** Request-record components carrying the same data. */
    private static final List<String> FORBIDDEN_RECORD_COMPONENTS = List.of(
            "email()",
            "phone()",
            "name()",
            "recipientName()",
            "addressLine1()",
            "addressLine2()",
            "postalCode()",
            "notes()",
            "password()",
            "refreshToken()");

    /** A log call and its full argument list, across line breaks. */
    private static final Pattern LOG_CALL =
            Pattern.compile("log\\.(trace|debug|info|warn|error)\\s*\\((.*?)\\);", Pattern.DOTALL);

    @Test
    void noLogStatementCarriesPersonalData() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(path -> {
                String source = read(path);
                Matcher matcher = LOG_CALL.matcher(source);
                while (matcher.find()) {
                    String arguments = matcher.group(2);
                    Stream.concat(FORBIDDEN_ACCESSORS.stream(), FORBIDDEN_RECORD_COMPONENTS.stream())
                            .filter(arguments::contains)
                            .forEach(forbidden -> violations.add(
                                    "%s logs %s in: %s".formatted(path, forbidden, collapse(arguments))));
                    if (PERSONAL_NAME_ACCESS.matcher(arguments).find()) {
                        violations.add("%s logs a person's name in: %s".formatted(path, collapse(arguments)));
                    }
                }
            });
        }

        assertThat(violations)
                .as("log statements must never carry personal data or credentials")
                .isEmpty();
    }

    /** The scan is worthless if the pattern silently matches nothing. */
    @Test
    void theScanActuallyFindsLogStatements() throws IOException {
        int found = 0;
        try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
            List<Path> javaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
            for (Path path : javaFiles) {
                Matcher matcher = LOG_CALL.matcher(read(path));
                while (matcher.find()) {
                    found++;
                }
            }
        }
        assertThat(found)
                .as("expected the source scan to find the project's log statements")
                .isGreaterThan(30);
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

    private static String collapse(String arguments) {
        return arguments.replaceAll("\\s+", " ").trim();
    }
}
