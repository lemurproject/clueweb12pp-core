'''
Heritrix core files
'''
import os


def job_warc_files(heritrix_job_path):
	for root, dirs, files in os.walk(heritrix_job_path):
		for filename in files:
			if filename.find('warc.gz') >= 0 and filename.find('latest') < 0:
				yield os.path.join(root, filename)

def is_response_record(record):
	return record.type == 'response'
