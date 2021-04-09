package com.techelevator.dao;

import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.techelevator.model.Visit;
import com.techelevator.model.VisitReason;

@Component
public class JDBCVisitDAO implements VisitDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCVisitDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Visit> getAllAvailableVisitsByDoctorId(int patientId, int doctorId, LocalDate dateOfVisit) {
		// TODO Auto-generated method stub
		List<Visit> listOfAvailableVisitsByDoctorId = new ArrayList<>();
		String selectSQL = "SELECT patient.patient_id, doctor_schedule.doctor_id, doctor_schedule.appointment_start_time FROM doctor_schedule " + 
				"RIGHT OUTER JOIN visit ON visit.doctor_id = doctor_schedule.doctor_id " + 
				"JOIN patient ON patient.doctor_id = doctor_schedule.doctor_id " + 
				"WHERE patient.patient_id = ? AND visit.doctor_id = ? AND (visit.date_of_visit = ? AND doctor_schedule.appointment_start_time " + 
				"NOT IN (SELECT visit.start_time FROM visit) OR ? NOT IN (SELECT visit.date_of_visit FROM visit)) " + 
				"GROUP BY patient.patient_id, doctor_schedule.doctor_schedule_id, doctor_schedule.appointment_start_time " + 
				"ORDER BY patient.patient_id, doctor_schedule.doctor_schedule_id; " ;
		SqlRowSet results = jdbcTemplate.queryForRowSet(selectSQL, patientId, doctorId, dateOfVisit, dateOfVisit);
		while (results.next()) {
			listOfAvailableVisitsByDoctorId.add(mapRowToAvailableVisits(results));
		}
		return listOfAvailableVisitsByDoctorId;
	}

	@Override
	public List<VisitReason> retrieveListOfVisitReasons() {
		List<VisitReason> listOfVisitReasons = new ArrayList<>();
		String seletSQL = "SELECT visit_reason_id, reason FROM visit_reason;";
		SqlRowSet result = jdbcTemplate.queryForRowSet(seletSQL);
		while (result.next()) {
			listOfVisitReasons.add(mapRowToVisitReasons(result));
		}
		
		return listOfVisitReasons;
	}
	
	@Override
	public Visit bookAppointment(Visit visit) {
		// TODO Auto-generated method stub
		String insertSql = "INSERT INTO visit(patient_id, doctor_id,  date_of_visit, start_time, status_id, visit_reason, description)  VALUES (?,?,?,?,?,?, ?); ";
		jdbcTemplate.update(insertSql, visit.getPatientId(), visit.getDoctorId(), visit.getDateOfVisit(), visit.getStartTime(), visit.getStatusId(), visit.getDescription(), visit.getVisitReason());
	
		return visit;
	}
	
	

	/** used to map the results row to properties of the venueSpace class */
	private Visit mapRowToAvailableVisits(SqlRowSet results) {
		Visit visit = new Visit();
		// only pull out what we need for our query
		// visit.setDateOfVisit(results.getDate("date_of_visit"));
		visit.setDoctorId(results.getInt("doctor_id"));
		//visit.setEndTime(results.getTime("appointment_end_time"));
		visit.setPatientId(results.getInt("patient_id"));
		visit.setStartTime(results.getTime("appointment_start_time"));
		// visit.setStatusId(results.getString("status_id"));
		// visit.setVisitId(results.getInt("visit_id"));

		return visit;

	}
	private VisitReason mapRowToVisitReasons(SqlRowSet result) {
	VisitReason visitReason = new VisitReason();
	visitReason.setReason(result.getString("reason"));
	visitReason.setVisitReasonId(result.getInt("visit_reason_id"));
	return visitReason;
	}
	

	@Override
	public List<Visit> retrieveListOfUpcomingAppointments() {
		// TODO Auto-generated method stub
		List<Visit> futureAppointmentsList = new ArrayList<>();

		String sql = "SELECT visit.date_of_visit, visit.start_time FROM visit WHERE visit.date_of_visit > (SELECT CURRENT_DATE) ORDER BY visit.date_of_visit, visit.start_time; ";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
		while (results.next()) {

			Visit visit = new Visit();
			visit.setStartTime(results.getTime("start_time"));
			//visit.setEndTime(results.getTime("end_time"));
			visit.setDateOfVisit(results.getDate("date_of_visit"));

			futureAppointmentsList.add(visit);
		}
		return futureAppointmentsList;
	}

	
}
