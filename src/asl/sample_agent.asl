// Agent sample_agent in project rideau_robocup

/* Initial beliefs and rules */

/* Initial goals */
!start.

/* Plans */
+!start : true <- look.
+fire <- move;!look.
+!look <- looked;!look.