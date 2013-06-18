'''
Core code to process the clueweb12++ dataset
'''

import datetime
import os
import urlparse
import warc

CLUEWEB12_CRAWL_START_TIME = datetime.datetime(2012, 01, 01)
CLUEWEB12_CRAWL_END_TIME   = datetime.datetime(2012, 06, 30)

def is_within_crawl_timespan(time_obj):
	return time_obj >= CLUEWEB12_CRAWL_START_TIME and time_obj <= CLUEWEB12_CRAWL_END_TIME

def job_warc_files(job_path):
	'''
	Return a list of warc files from a heritrix job directory
	'''
	for root, dirs, files in os.walk(job_path):
		for filename in files:
			if filename.find('warc.gz') >= 0 and root.find('latest') < 0:
				yield os.path.join(root, filename)

def handle_warc_file(warc_file, f):
	'''
	Perform a function over every record in a warc file.
	Args:
		- warc_file : path/to/warc.gz file
		- f         : function to run over every record.
	'''
	warc_handle = warc.open(warc_file)

	for record in warc_handle:
		yield f(record)

def get_response_warcs(warc_file):
	'''
	Returns a sequence of response records only in a warc_file
	Args:
		- warc_file : path/to/warc.gz file
	'''
	warc_handle = warc.open(warc_file)

	for record in warc_handle:
		if record.type == 'response':
			yield record

def get_content_warcs(warc_file):
	'''
	Return a collection of content warcs:
	A content warc is one that contains text/html content
	'''
	for record in get_response_warcs(warc_file):
		parsed = urlparse.urlparse(record.url)
		if parsed.scheme != 'dns' and parsed.path.find('robots.txt') < 0:
			yield record

def sample_documents(warc_file, dest_file_prefix, num_docs = 2):
	'''
	Produces num_docs document samples
	'''
	for record in get_content_warcs(warc_file):
		if num_docs == 0:
			return
		record_payload = record.payload.read()

		if record_payload.splitlines()[0].find('200') >= 0:
			dest_file = dest_file_prefix + '-' + str(num_docs) + '.txt'
			with open(dest_file, 'w+') as dest_handle:
				dest_handle.write(record_payload)
			num_docs -= 1