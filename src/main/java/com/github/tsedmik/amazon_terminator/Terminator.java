package com.github.tsedmik.amazon_terminator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

/**
 * Terminate all JBoss Fuse fabric instances on Amazon EC2 Cloud
 * 
 * @author tsedmik
 */
public class Terminator {
	
	private static final Logger log = Logger.getLogger(Terminator.class.toString());

	public static void main(String[] args) {

		if (args.length != 2) {
			log.severe("Amazon EC2 ID and Access key must be set!");
			return;
		}
			
		// connect into the Amazon EC2
		AWSCredentials credentials = new BasicAWSCredentials(args[0], args[1]);
		AmazonEC2Client client = new AmazonEC2Client(credentials);
		client.setEndpoint("ec2.eu-west-1.amazonaws.com");

		// get instances with security group "jclouds#fabrics"
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		List<String> values = new ArrayList<String>();
		values.add("jclouds#fabric");
		Filter filter = new Filter("group-name", values);
		DescribeInstancesResult result;

		// checks credentials
		try {
			result = client.describeInstances(request.withFilters(filter));
		} catch (AmazonServiceException ex) {
			log.severe("Provided credentials are wrong!");
			return;
		}
		
		List<String> instanceIDs = new ArrayList<String>();
		List<Reservation> reservations = result.getReservations();
		for (Reservation reservation : reservations) {

			List<Instance> instances = reservation.getInstances();
			for (Instance instance : instances) {

				instanceIDs.add(instance.getInstanceId());
				log.info(instance.getInstanceId() + " : " + instance.getState());
            }
		}
		
		// terminate instances
		if (!instanceIDs.isEmpty()) {
			TerminateInstancesResult terminationResult = client.terminateInstances(new TerminateInstancesRequest(instanceIDs));
			log.info(terminationResult.toString());
		} else {
			log.info("Nothing to terminate.");
		}
	}
}