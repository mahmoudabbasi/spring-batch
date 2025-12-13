package com.example.batchprocessing.service.catalog.config;


import com.example.batchprocessing.service.catalog.domain.Product;
import com.example.batchprocessing.service.catalog.dto.ProductDTO;
import com.example.batchprocessing.service.catalog.dto.ProductWrapper;
import com.example.batchprocessing.service.catalog.error.ExceptionSkipPolicy;
import com.example.batchprocessing.service.catalog.listener.ProductImportJobCompletionListener;
import com.example.batchprocessing.service.catalog.listener.StepSkipListener;
import com.example.batchprocessing.service.catalog.mapper.ProductFieldSetMapper;
import com.example.batchprocessing.service.catalog.partition.ProductDataPartitioner;
import com.example.batchprocessing.service.catalog.processor.ProductItemProcessor;
import com.example.batchprocessing.service.catalog.writer.ProductItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ProductBatchConfiguration {

    private static final String FIRST_STEP = "step-1";

    private static final String JOB_NAME = "productImportJob";

    @Bean
    public ItemReader<ProductDTO> productItemReader() {
        FlatFileItemReader<ProductDTO> reader = new FlatFileItemReader<>();
//        reader.setStrict(false); // default: true

        reader.setResource(new ClassPathResource("data/product_catalog_mini.csv"));
        reader.setLinesToSkip(1);
        reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());
        reader.setLineMapper(new DefaultLineMapper<ProductDTO>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("productId", "productName", "productBrand", "price", "description");
                setDelimiter(",");
                setQuoteCharacter('\"');
                setStrict(false);
            }});
            setFieldSetMapper(new ProductFieldSetMapper());
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<ProductDTO, ProductWrapper> productItemProcessor() {
        return new ProductItemProcessor();
    }

    /**
     * Partition the product data
     *
     * @return ProductDataPartitioner
     */
    @Bean
    public ProductDataPartitioner productDataPartitioner() {
        return new ProductDataPartitioner();
    }

    /**
     * Handle the partitioned data
     *
     * @param jobRepository JobRepository
     * @return PartitionHandler
     */
    @Bean
    public PartitionHandler partitionHandler(JobRepository jobRepository) {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(3);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep(jobRepository));
        return taskExecutorPartitionHandler;
    }

    /**
     * Create the slave step to process the product data
     *
     * @param jobRepository JobRepository
     * @return Step
     */
    @Bean
    public Step slaveStep(JobRepository jobRepository) {
        return new StepBuilder("slaveStep", jobRepository)
                .<ProductDTO, ProductWrapper>chunk(10, transactionManager())
                .reader(productItemReader())
                .processor(productItemProcessor())
                .writer(productItemWriter())
                .faultTolerant()
                .listener(stepSkipListener())
                .skipPolicy(new ExceptionSkipPolicy())
                .build();
    }

    /**
     * Create the master step to partition the product data
     *
     * @param jobRepository JobRepository
     * @return Step
     */
    @Bean
    public Step masterStep(JobRepository jobRepository) {
        return new StepBuilder("masterStep", jobRepository).
                partitioner(slaveStep(jobRepository).getName(), productDataPartitioner())
                .partitionHandler(partitionHandler(jobRepository))
                .build();
    }

    /**
     * Write the product data to the database
     *
     * @return ProductItemWriter
     */
    @Bean
    public ProductItemWriter productItemWriter() {
        return new ProductItemWriter();
    }

    /**
     * Create the job to import the product data
     *
     * @param jobRepository JobRepository
     * @param listener      ProductImportJobCompletionListener
     * @return Job
     */
    @Bean("productImportJob")
    public Job productImportJob(JobRepository jobRepository, ProductImportJobCompletionListener listener) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(listener)
                .start(masterStep(jobRepository))
                .build();
    }

    /**
     * Create the task executor
     *
     * @return TaskExecutor
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setQueueCapacity(4);
        return taskExecutor;
    }

    /**
     * Create the transaction manager
     *
     * @return PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }

    /**
     * Create the step skip listener
     *
     * @return StepSkipListener
     */
    @Bean
    public StepSkipListener stepSkipListener() {
        return new StepSkipListener();
    }
}
