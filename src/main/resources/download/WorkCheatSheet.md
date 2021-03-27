==============================> Kubernetes <==============================
watch kubectl get pods -l name=clients-api -n prod
watch kubectl get pods -l name=confserver -n prod

kubectl get pod -n prod
kubectl logs pod -n prod

##### secor
kubectl get pods - secor

kubectl describe pod -n prod -l name=shib-platform
kubectl logs <pod_name> -n prod -c <container_name>

kubectl apply -f applications/prod/shib-platform/manifest.yaml

##### info about mapped door for the service inside worker
kubectl get svc ranking-api -n prod

#### get pods
watch kubectl get pods -l name=<pod_names> -n prod

###### get pods name
kubectl get pod -n prod | grep shib

###### enter pod
kubectl exec --stdin --tty -n prod <pod_name> -- /bin/sh


=========================> Authentication Shib <=========================

git checkout - <branch_name>
git add -p (y)
git commit -m "<syntax>: <message>"
git push origin <branch_name>

git checkout master
git pull
git tag -a <tag_name> -m "<message>"
git push origin <tag_name>


================================> Chef ================================
#### chef
knife ec2 server delete <ec2_instance_number> --node-name <ec2_instance_name> --purge
knife role from file roles/bigdata-storm-worker.rb
knife role show bigdata-storm-worker

#### to give permission to machines in chef
sudo chef-client -o 'recipe[chaordic-users::platform]'


================================> ETL ================================

cat  /dashboard-data/etl-datalogs/logs
ll   /dashboar-data/etl-data/etl4-data/<data>/kpis

yarn logs -applicationId <application_number> | grep -i exception

ssh mhirleylopes@etl4-onsite.chaordicsystems.com
tail -f /tmp/etl-T.log
tail -f /tmp/etl-L.log

ll -h /dashboard-data/etl-data/etl4-data/<etl_date>/kpis
kubectl logs -n prod -l name=dashboard-api

sudo su - polvo
sudo kill

================================> Cassandra ================================
### run cassandra locally
make docker.up.devmode

### login ssh cassandra cas-batch-c-07 (172.28.13.254)
cqshl 172.28.13.254

use yaca;
desc tables;
desc table <table_name>;
 - check for schema to be updated 
insert into <table_name> (<cols>) values (<values>);

================================> Ansible ================================
### activate venv
source venv/bin/venv activate

### for luigi, jenkins and yarn (master): after chef
ansible-playbook -v <playbook_name> --diff -u mhirleylopes --limit <ip_inventory> -C (not applied, just checking mock)

##change memory allocation
export JAVA_OPTIONS=XXXXXX


ansible-playbook <playbook_name> --check --diff (not applied)
ansible-playbook <playbook_name> --diff -v (applied)


================================> Luigi/Monit ================================
sudo monit stop <process_name>
sudo monit start <process_name>


================================> logstash ================================
ssh mhirleylopes@platform.logstash-01

