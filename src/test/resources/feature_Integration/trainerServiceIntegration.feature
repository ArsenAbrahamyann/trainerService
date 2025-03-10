Feature: TrainerService JMS Communication
  As a service
  I want to receive messages from GymService via ActiveMQ
  So that I can process workloads and provide responses

  Background:
    Given the ActiveMQ broker is running

  Scenario: Receive training update from gym service
    When the trainerService receives a training update message
    Then the trainer workload should be updated

  Scenario: Provide training hours response to gym service
    When the trainerService receives a training hours request message
    Then the trainerService should send a training hours response