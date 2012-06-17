package com.singularity.clover.babysitter.notification;

public interface RocketLauncher<DataType,Attribute> {
	boolean launch(Notification notification,Attribute attr);
	String serialized(Notification notification);
	boolean deserialized(DataType data,Notification notification);
}
