package net.azisaba.papermixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;

public class LoggerAdapterSlf4j extends LoggerAdapterAbstract {
    
    private static final org.slf4j.event.Level[] LEVELS = {
        /* FATAL = */ org.slf4j.event.Level.ERROR,
        /* ERROR = */ org.slf4j.event.Level.ERROR,
        /* WARN =  */ org.slf4j.event.Level.WARN,
        /* INFO =  */ org.slf4j.event.Level.INFO,
        /* DEBUG = */ org.slf4j.event.Level.DEBUG,
        /* TRACE = */ org.slf4j.event.Level.TRACE
    };
    
    private final Logger logger;

    public LoggerAdapterSlf4j(String name) {
        super(name);
        this.logger = LoggerFactory.getLogger(name);
    }
    
    @Override
    public String getType() {
        return "Slf4j (via Paper)";
    }

    @Override
    public void catching(Level level, Throwable t) {
        log(level, "catching: ", t);
    }

    @Override
    public void catching(Throwable t) {
        this.logger.warn("catching: ", t);
    }

    @Override
    public void debug(String message, Object... params) {
        this.logger.debug(message, params);
    }

    @Override
    public void debug(String message, Throwable t) {
        this.logger.debug(message, t);
    }

    @Override
    public void error(String message, Object... params) {
        this.logger.error(message, params);
    }

    @Override
    public void error(String message, Throwable t) {
        this.logger.error(message, t);
    }

    @Override
    public void fatal(String message, Object... params) {
        this.logger.error(message, params);
    }

    @Override
    public void fatal(String message, Throwable t) {
        this.logger.error(message, t);
    }

    @Override
    public void info(String message, Object... params) {
        this.logger.info(message, params);
    }

    @Override
    public void info(String message, Throwable t) {
        this.logger.info(message, t);
    }

    @Override
    public void log(Level level, String message, Object... params) {
        switch (level) {
            case FATAL, ERROR -> logger.error(message, params);
            case WARN -> logger.warn(message, params);
            case INFO -> logger.info(message, params);
            case DEBUG -> logger.debug(message, params);
            case TRACE -> logger.trace(message, params);
        }
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        switch (level) {
            case FATAL, ERROR -> logger.error(message, t);
            case WARN -> logger.warn(message, t);
            case INFO -> logger.info(message, t);
            case DEBUG -> logger.debug(message, t);
            case TRACE -> logger.trace(message, t);
        }
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        this.logger.error("throwing: ", t);
        return t;
    }

    @Override
    public void trace(String message, Object... params) {
        this.logger.trace(message, params);
    }

    @Override
    public void trace(String message, Throwable t) {
        this.logger.trace(message, t);
    }

    @Override
    public void warn(String message, Object... params) {
        this.logger.warn(message, params);
    }

    @Override
    public void warn(String message, Throwable t) {
        this.logger.warn(message, t);
    }

}
