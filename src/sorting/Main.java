package sorting;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class InnerException extends RuntimeException {
    public InnerException(String message) {
        super(message);
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            DataType dataType = DataType.LONG;
            SortingType sortingType = SortingType.NATURAL;
            InputStream inputStream = System.in;
            PrintStream outputStream = System.out;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    String token = args[i];
                    try {
                        if (token.startsWith("-")) {
                            switch (token) {
                                case "-dataType":
                                    if (i + 1 < args.length) {
                                        switch (args[i + 1]) {
                                            case "long":
                                                dataType = DataType.LONG;
                                                break;
                                            case "word":
                                                dataType = DataType.WORD;
                                                break;
                                            case "line":
                                                dataType = DataType.LINE;
                                                break;
                                            default:
                                                throw new RuntimeException("Unknown data type: " + args[i + 1]);
                                        }
                                    } else {
                                        throw new RuntimeException("No data type defined!");
                                    }
                                    i++;
                                    break;
                                case "-sortingType":
                                    if (i + 1 < args.length) {
                                        switch (args[i + 1]) {
                                            case "natural":
                                                sortingType = SortingType.NATURAL;
                                                break;
                                            case "byCount":
                                                sortingType = SortingType.BY_COUNT;
                                                break;
                                            default:
                                                throw new RuntimeException("Unknown sorting type: " + args[i + 1]);
                                        }
                                    } else {
                                        throw new RuntimeException("No sorting type defined!");
                                    }
                                    i++;
                                    break;
                                case "-inputFile":
                                    if (i + 1 < args.length) {
                                        inputStream = new FileInputStream(args[i + 1]);
                                    }
                                    i++;
                                    break;
                                case "-outputFile":
                                    if (i + 1 < args.length) {
                                        outputStream = new PrintStream(new FileOutputStream(args[i + 1]));
                                    }
                                    i++;
                                    break;
                                default:
                                    throw new InnerException(
                                            String.format("\"%s\" isn't a valid parameter. It's skipped.", token)
                                    );
                            }
                        } else {
                            throw new RuntimeException("Unexpected token as parameter: " + token);
                        }
                    } catch (InnerException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            Context context = new Context(dataType, sortingType, inputStream, outputStream);
            context.execute();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e. getMessage());
        }
    }
}

enum DataType {
    LINE,
    WORD,
    LONG
}

enum SortingType {
    NATURAL,
    BY_COUNT
}

class Context {
    private DataStatMethod method;

    public Context(DataType dataType, SortingType sortingType, InputStream inputStream, PrintStream outputStream) {
        switch (dataType) {
            case LONG:
                this.method = new LongStat(sortingType, inputStream, outputStream);
                break;
            case WORD:
                this.method = new WordStat(sortingType, inputStream, outputStream);
                break;
            case LINE:
                this.method = new LineStat(sortingType, inputStream, outputStream);
                break;
            default:
                throw new RuntimeException("Unknown data type: " + dataType);
        }
    }

    public void execute() {
        method.invokeMethod();
    }
}

interface DataStatMethod {
    void invokeMethod();
}

abstract class DataStat implements DataStatMethod {
    protected SortingType sortingType;
    protected InputStream inputStream;
    protected PrintStream outputStream;

    public DataStat() {
        this.sortingType = SortingType.NATURAL;
    }

    public DataStat(SortingType sortingType) {
        this.sortingType = sortingType;
    }

    public DataStat(SortingType sortingType, InputStream inputStream, PrintStream outputStream) {
        this.sortingType = sortingType;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
}

class LongStat extends DataStat {

    public LongStat() {
        super();
    }

    public LongStat(SortingType sortingType) {
        super(sortingType);
    }

    public LongStat(SortingType sortingType, InputStream inputStream, PrintStream outputStream) {
        super(sortingType, inputStream, outputStream);
    }

    @Override
    public void invokeMethod() {
        Scanner scanner = new Scanner(inputStream);
        List<Long> longs = new ArrayList<>();
        while (scanner.hasNext()) {
            String token = scanner.next();
            try {
                longs.add(Long.parseLong(token));
            } catch (NumberFormatException e) {
                System.out.printf("\"%s\" isn't a long. It's skipped.%n", token);
            }
        }
        outputStream.printf("Total numbers: %d\n", longs.size());
        switch (sortingType) {
            case NATURAL:
                longs.sort(Comparator.naturalOrder());
                String sortedData = longs.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(" "));
                outputStream.printf("Sorted data: %s%n", sortedData);
                break;
            case BY_COUNT:
                var map = longs.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                final int totalCount = longs.size();
                map.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .sorted(Map.Entry.comparingByValue())
                        .forEach(entry -> {
                            outputStream.printf("%d: %d time(s), %d%%%n",
                                    entry.getKey(), entry.getValue(),
                                    (int) Math.round((double) entry.getValue() / totalCount * 100.0)
                            );
                        });
                break;
        }
        scanner.close();
    }
}

class WordStat extends DataStat {

    public WordStat() {
    }

    public WordStat(SortingType sortingType) {
        super(sortingType);
    }

    public WordStat(SortingType sortingType, InputStream inputStream, PrintStream outputStream) {
        super(sortingType, inputStream, outputStream);
    }

    @Override
    public void invokeMethod() {
        Scanner scanner = new Scanner(inputStream);
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\S+");
        while (scanner.hasNext(pattern)) {
            words.add(scanner.next(pattern));
        }
        outputStream.printf("Total words: %d\n", words.size());
        switch (sortingType) {
            case NATURAL:
                words.sort(Comparator.naturalOrder());
                String sortedData = String.join(" ", words);
                outputStream.printf("Sorted data: %s%n", sortedData);
                break;
            case BY_COUNT:
                var map = words.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                final int totalCount = words.size();
                map.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .sorted(Map.Entry.comparingByValue())
                        .forEach(entry -> {
                            outputStream.printf("%s: %d time(s), %d%%%n",
                                    entry.getKey(), entry.getValue(),
                                    (int) Math.round((double) entry.getValue() / totalCount * 100.0)
                            );
                        });
                break;
        }
        scanner.close();
    }
}

class LineStat extends DataStat {

    public LineStat() {
    }

    public LineStat(SortingType sortingType) {
        super(sortingType);
    }

    public LineStat(SortingType sortingType, InputStream inputStream, PrintStream outputStream) {
        super(sortingType, inputStream, outputStream);
    }

    @Override
    public void invokeMethod() {
        Scanner scanner = new Scanner(inputStream);
        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        outputStream.printf("Total lines: %d\n", lines.size());
        switch (sortingType) {
            case NATURAL:
                lines.sort((s1, s2) -> -Integer.compare(s1.length(), s2.length()));
                outputStream.println("Sorted data:");
                lines.forEach(System.out::println);
                break;
            case BY_COUNT:
                var map = lines.stream()
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                final int totalCount = lines.size();
                map.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .sorted(Map.Entry.comparingByValue())
                        .forEach(entry -> {
                            outputStream.printf("%s: %d time(s), %d%%%n",
                                    entry.getKey(), entry.getValue(),
                                    (int) Math.round((double) entry.getValue() / totalCount * 100.0)
                            );
                        });
                break;
        }
        scanner.close();
    }
}
