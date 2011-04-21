package cz.poptavka.sample.domain.settings;

import cz.poptavka.sample.domain.common.DomainObject;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

/**
 * @author Juraj Martinka
 *         Date: 11.4.11
 */
@Entity
public class NotificationItem extends DomainObject {
    private boolean enabled;

    @Enumerated(value = EnumType.STRING)
    private Period period;

    @ManyToOne
    private Notification notification;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("NotificationItem");
        sb.append("{enabled=").append(enabled);
        sb.append(", period=").append(period);
        sb.append(", notification=").append(notification);
        sb.append('}');
        return sb.toString();
    }
}