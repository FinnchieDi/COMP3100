# The Psuedocode for Stage 2

# Checklist
- Edit LRR
- Use GETS Avail
- When no servers are available, put jobs in the global queue (ENQJ GQ) This will add it into a queue
- run until recieve a JCPL to say that room has been opened in a server, using (DEQJ GQ queueID [place of next job]) and assign them a server
- to find if there are no jobs left use (LSTQ #) to find the number of jobs in the queue, if there are none left, and all jobs are complete, quit the program
- when a new job is availble and there are servers avaiable for those servers, make sure that i