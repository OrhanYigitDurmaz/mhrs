package com.mhrs.appointment.application;

import com.mhrs.appointment.application.command.AdminOverrideAppointmentCommand;
import com.mhrs.appointment.application.command.CancelAppointmentCommand;
import com.mhrs.appointment.application.command.CreateAppointmentCommand;
import com.mhrs.appointment.application.command.RescheduleAppointmentCommand;
import com.mhrs.appointment.application.query.AppointmentSearchQuery;
import com.mhrs.appointment.domain.Appointment;
import java.util.List;

public interface AppointmentUseCase {
    Appointment createAppointment(CreateAppointmentCommand command);

    List<Appointment> listAppointments(AppointmentSearchQuery query);

    Appointment getAppointment(String appointmentId);

    Appointment confirmAppointment(String appointmentId);

    Appointment cancelAppointment(
        String appointmentId,
        CancelAppointmentCommand command
    );

    Appointment rescheduleAppointment(
        String appointmentId,
        RescheduleAppointmentCommand command
    );

    Appointment completeAppointment(String appointmentId);

    Appointment noShowAppointment(String appointmentId);

    Appointment adminOverrideAppointment(
        String appointmentId,
        AdminOverrideAppointmentCommand command
    );
}
