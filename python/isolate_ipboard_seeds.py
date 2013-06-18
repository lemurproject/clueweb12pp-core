'''
Code to isolate IP.Board seeds
'''

import argparse
from heritrix import crawl_log

def get_seeds(urls_list, heritrix_job):

	target_src_map = crawl_log.process_heritrix_job(heritrix_job)

	final_seeds = []

	urls = []
	with open(urls_list, 'r') as urls_list_handle:
		for url in urls_list_handle:
			urls.append(url.strip())

	for url in urls:
		import ipdb
		ipdb.set_trace()
		final_parent = get_final_ancestor(url.strip(), urls, target_src_map)

		final_seeds.append(final_parent)

	return final_seeds

def get_final_ancestor(url, urls_list, target_src_map):
	while True:
		if not target_src_map.has_key(url) and url in urls_list:
			return url

		elif target_src_map.has_key(url) and target_src_map[url] in urls_list:
			url = target_src_map[url]

		else:
			return url

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('urls_list', metavar = 'urls-list', help = 'file containing urls found')
		parser.add_argument('heritrix_job', metavar = 'heritrix-job', help = 'heritrix job directory')

		return parser.parse_args()

	parsed = parse_cmdline_args()
	import ipdb
	ipdb.set_trace()
	seeds = get_seeds(parsed.urls_list, parsed.heritrix_job)
	import ipdb
	ipdb.set_trace()