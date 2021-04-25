val lambda: (Long, Long) -> Long = { a, b -> (a..b).reduce { result, it -> result * it } }
